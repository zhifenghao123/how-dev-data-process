package com.howdev.flinkdev.biz.logstat.main;

import com.howdev.common.util.JacksonUtil;
import com.howdev.flinkdev.biz.logstat.domain.LogRecord;
import com.howdev.flinkdev.biz.logstat.dto.AggregateDimension;
import com.howdev.flinkdev.biz.logstat.dto.AggregateMetric;
import com.howdev.flinkdev.biz.logstat.dto.AggregateResult;
import com.howdev.flinkdev.biz.logstat.myoperator.aggregate.LogAggregateFunction;
import com.howdev.flinkdev.biz.logstat.myoperator.aggregate.LogPreAggregateFunction;
import com.howdev.flinkdev.biz.logstat.myoperator.processWindow.LogPreProcessWindowFunction;
import com.howdev.flinkdev.biz.logstat.myoperator.processWindow.LogProcessWindowFunction;
import com.howdev.flinkdev.flink.watermark.MyWatermarkStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.OutputTag;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LogStatMain {
    public static void main(String[] args) throws Exception {
        // 创建流执行环境
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 创建带有WebUI的本地流执行环境
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);


        // 创建自定义的Socket源
        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据
        DataStreamSource<String> socketDataStream = env.socketTextStream("127.0.0.1", 9999);

        FilterFunction<String> filterFunction = new FilterFunction<String>() {
            @Override
            public boolean filter(String value) throws Exception {
                //LogRecord logRecord = JacksonUtil.fromJson(value, LogRecord.class);
                //return !Objects.isNull(logRecord);

                if (value.isEmpty()) {
                    return false;
                }
                String[] split = value.split(",");
                return split.length >= 6;
            }
        };

        MapFunction<String, LogRecord> mapFunction = new MapFunction<String, LogRecord>() {
            @Override
            public LogRecord map(String value) throws Exception {
                //LogRecord logRecord = JacksonUtil.fromJson(value, LogRecord.class);

                String[] split = value.split(",");

                LogRecord logRecord = new LogRecord();
                logRecord.setRequestId(split[0]);
                logRecord.setRequestTimeStamp(Long.parseLong(split[1]) * 1000L);
                logRecord.setService(split[2]);
                logRecord.setServiceIp(split[3]);
                logRecord.setMethod(split[4]);
                logRecord.setReturnCode(split[5]);

                int randomNum = (int) (Math.random() * 4) + 1;
                logRecord.setRandomNum(randomNum);
                System.out.println(JacksonUtil.toJson(logRecord));
                return logRecord;
            }
        };

        MyWatermarkStrategy<LogRecord> watermarkStrategy = new MyWatermarkStrategy<>(1000L);

        SingleOutputStreamOperator<LogRecord> logRecordDataStream = socketDataStream
                .filter(filterFunction)
                .map(mapFunction).disableChaining();

        SingleOutputStreamOperator<LogRecord> assignTimestampsAndWatermarkDataStream =
                logRecordDataStream
                        .assignTimestampsAndWatermarks(watermarkStrategy.withIdleness(Duration.ofSeconds(5)));

        KeySelector<LogRecord, Tuple5<String, String, String, String, Integer>> preAggKeySelector = new KeySelector<LogRecord, Tuple5<String, String, String, String, Integer>>() {
            @Override
            public Tuple5<String, String, String, String, Integer> getKey(LogRecord value) throws Exception {
                return Tuple5.of(value.getService(), value.getServiceIp(), value.getMethod(), value.getReturnCode(), value.getRandomNum());
            }
        };

        // 预聚合（针对数据倾斜）
        SingleOutputStreamOperator<AggregateResult<AggregateDimension, AggregateMetric>> preAggregateDataStream = assignTimestampsAndWatermarkDataStream
                .keyBy(preAggKeySelector)
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .aggregate(new LogPreAggregateFunction(), new LogPreProcessWindowFunction());


        KeySelector<AggregateResult<AggregateDimension, AggregateMetric>, Tuple3<Long, String, String>> keySelector = new KeySelector<AggregateResult<AggregateDimension, AggregateMetric>, Tuple3<Long, String, String>>() {
            @Override
            public Tuple3<Long, String, String> getKey(AggregateResult<AggregateDimension, AggregateMetric> value) throws Exception {
                return Tuple3.of(value.getRequestTimeStamp(), value.getDimension().getService(), value.getDimension().getServiceIp());
            }
        };

        OutputTag<AggregateResult<AggregateDimension, AggregateMetric>> outPutTag =
                new OutputTag<>(
                        "drillDownAggregateResult",
                        TypeInformation.of(new TypeHint<AggregateResult<AggregateDimension, AggregateMetric>>() {})
                );

        SingleOutputStreamOperator<AggregateResult<AggregateDimension, AggregateMetric>> aggregateDataStream = preAggregateDataStream
                .keyBy(keySelector)
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .aggregate(new LogAggregateFunction(), new LogProcessWindowFunction(outPutTag));


        aggregateDataStream
                .print("DETECT:");

        aggregateDataStream
                .getSideOutput(outPutTag)
                .filter(value -> {
                    // 获取分钟数并判断是否为奇数分钟, 如果是奇数分钟则进行数据的存储
                    long minute = TimeUnit.MILLISECONDS.toMinutes(value.getRequestTimeStamp());
                    log.info("minute: {}", minute);
                    return minute % 2 != 0;
                })
                .print("DRILL_DOWN:");


        env.execute();

    }
}
