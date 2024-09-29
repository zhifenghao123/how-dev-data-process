package com.howdev.flinkdev.log.main;

import com.howdev.common.util.JacksonUtil;
import com.howdev.flinkdev.log.operator.MyAggregateFunction;
import com.howdev.flinkdev.log.operator.MyProcessWindowFunction;
import com.howdev.flinkdev.log.watermark.MyWatermarkStrategy;
import com.howdev.flinkdev.log.biz.domain.LogRecord;
import com.howdev.flinkdev.log.biz.dto.LogRecordAggregateResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class LogDataSkew {
    public static void main(String[] args) throws Exception {
        // 创建流执行环境
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 创建带有WebUI的本地流执行环境
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);


        // 创建自定义的Socket源
        DataStreamSource<String> socketDataStream = env.socketTextStream("127.0.0.1", 9999);

        FilterFunction<String> filterFunction = new FilterFunction<String>() {
            @Override
            public boolean filter(String value) throws Exception {
                LogRecord logRecord = JacksonUtil.fromJson(value, LogRecord.class);
                return !Objects.isNull(logRecord);
            }
        };
        MapFunction<String, LogRecord> mapFunction = new MapFunction<String, LogRecord>() {
            @Override
            public LogRecord map(String value) throws Exception {
                LogRecord logRecord = JacksonUtil.fromJson(value, LogRecord.class);

                int randomNum = (int) (Math.random() * 20) + 1;
                logRecord.setRandomNum(randomNum);

                //System.out.println(JacksonUtil.toJson(logRecord));

                return logRecord;
            }
        };

        MyWatermarkStrategy watermarkStrategy = new MyWatermarkStrategy();

        SingleOutputStreamOperator<LogRecord> logRecordDataStream = socketDataStream
                .filter(filterFunction)
                .map(mapFunction).disableChaining();


        SingleOutputStreamOperator<LogRecord> assignTimestampsAndWatermarkDataStream =
                logRecordDataStream
                        .assignTimestampsAndWatermarks(watermarkStrategy.withIdleness(Duration.ofSeconds(5)));

        KeySelector<LogRecord, Tuple3<String, String, Integer>> keySelector = new KeySelector<LogRecord, Tuple3<String, String, Integer>>() {
            @Override
            public Tuple3<String, String, Integer> getKey(LogRecord value) throws Exception {
                Tuple3<String, String, Integer> key = new Tuple3<>(value.getService(), value.getMethod(), value.getRandomNum());
                log.info("KeySelector key: {}", key);
                return key;
            }
        };

        SingleOutputStreamOperator<LogRecordAggregateResult> intermediateAggregateDataStream = assignTimestampsAndWatermarkDataStream.keyBy(keySelector)
                .window(TumblingEventTimeWindows.of(Time.seconds(30)))
                .aggregate(new MyAggregateFunction(), new MyProcessWindowFunction());


        intermediateAggregateDataStream.addSink(new PrintSinkFunction<>(true)).name("intermediateAggregateDataStream");

        KeySelector<LogRecordAggregateResult, Tuple3<Long, String, String>> keySelector2 = new KeySelector<LogRecordAggregateResult, Tuple3<Long, String, String>>() {
            @Override
            public Tuple3<Long, String, String> getKey(LogRecordAggregateResult value) throws Exception {
                Tuple3<Long, String, String> key = new Tuple3<>(value.getRequestMinuteTimeStamp(), value.getService(), value.getMethod());
                log.info("KeySelector2 key: {}", key);
                return key;
            }
        };

        SingleOutputStreamOperator<LogRecordAggregateResult> finalAggregateDataStream = intermediateAggregateDataStream.keyBy(keySelector2)
                .reduce(new ReduceFunction<LogRecordAggregateResult>() {
                    @Override
                    public LogRecordAggregateResult reduce(LogRecordAggregateResult value1, LogRecordAggregateResult value2) throws Exception {
                        int totalCount = value1.getCount() + value2.getCount();

                        Map<Integer, Integer> codeExtra1 = value1.getCodeToCountMap();
                        Map<Integer, Integer> codeExtra2 = value2.getCodeToCountMap();
                        Map<Integer, Integer> mergedMap = Stream.concat(
                                        codeExtra1.entrySet().stream(),
                                        codeExtra2.entrySet().stream()
                                )
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> Optional.ofNullable(entry.getValue()).orElse(0), // 将null视为0
                                        Integer::sum
                                ));

                        long totalTime = value1.getTotalTime() + value2.getTotalTime();

                        LogRecordAggregateResult newLogRecordAggregateResult = new LogRecordAggregateResult();
                        newLogRecordAggregateResult.setRequestMinuteTimeStamp(value1.getRequestMinuteTimeStamp());
                        newLogRecordAggregateResult.setRequestMinuteTime(value1.getRequestMinuteTime());
                        newLogRecordAggregateResult.setService(value1.getService());
                        newLogRecordAggregateResult.setMethod(value1.getMethod());

                        newLogRecordAggregateResult.setCount(totalCount);
                        newLogRecordAggregateResult.setCodeToCountMap(mergedMap);

                        newLogRecordAggregateResult.setTotalTime(totalTime);

                        return newLogRecordAggregateResult;
                    }
                });

        finalAggregateDataStream.addSink(new PrintSinkFunction(true)).name("finalAggregateDataStream");

        env.execute();

    }

}
