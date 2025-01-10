package com.howdev.flinkdev.log.main;

import com.howdev.common.util.JacksonUtil;
import com.howdev.flinkdev.log.operator.MyAggregateFunction;
import com.howdev.flinkdev.log.operator.MyProcessWindowFunction;
import com.howdev.flinkdev.log.watermark.MyWatermarkStrategy;
import com.howdev.flinkdev.log.biz.domain.LogRecord;
import com.howdev.flinkdev.log.biz.dto.LogRecordAggregateResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class LogDataSkew2 {
    public static void main(String[] args) throws Exception {
        // 创建流执行环境
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 创建带有WebUI的本地流执行环境
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);


        // 创建自定义的Socket源
        DataStreamSource<String> dataStreamSource = env.socketTextStream("127.0.0.1", 9999);

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

        SingleOutputStreamOperator<LogRecord> logRecordSingleOutputStreamOperator = dataStreamSource
                .filter(filterFunction)
                .map(mapFunction).disableChaining()
                .assignTimestampsAndWatermarks(watermarkStrategy.withIdleness(Duration.ofSeconds(5)));

        KeySelector<LogRecord, Tuple3<String, String, Integer>> keySelector = new KeySelector<LogRecord, Tuple3<String, String, Integer>>() {
            @Override
            public Tuple3<String, String, Integer> getKey(LogRecord value) throws Exception {
                Tuple3<String, String, Integer> key = new Tuple3<>(value.getService(), value.getMethod(), value.getRandomNum());
                //log.info("KeySelector key: {}", key);
                return key;
            }
        };

        SingleOutputStreamOperator<LogRecordAggregateResult> aggregateDataStream = logRecordSingleOutputStreamOperator.keyBy(keySelector)
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .aggregate(new MyAggregateFunction(), new MyProcessWindowFunction());

        //aggregateDataStream.addSink(new PrintSinkFunction<>("aggregateDataStream"));
        String insertSql = "insert into log_record_intermediate " +
                "(requestMinuteTimeStamp, requestMinuteTime, service, method, count, codeToCountMap, totalTime, avgTime)" +
                " values (?, ?, ?, ?, ?, ?, ?, ?)";
        aggregateDataStream.addSink(
                JdbcSink.sink(
                        insertSql,
                        new JdbcStatementBuilder<LogRecordAggregateResult>() {
                            @Override
                            public void accept(PreparedStatement preparedStatement, LogRecordAggregateResult logRecordAggregateResult) throws SQLException {
                                Date requestMinuteTime = logRecordAggregateResult.getRequestMinuteTime();
                                String formattedRequestMinuteTime = DateFormatUtils.format(requestMinuteTime, "yyyy-MM-dd HH:mm:ss");

                                preparedStatement.setLong(1, logRecordAggregateResult.getRequestMinuteTimeStamp());
                                preparedStatement.setString(2, formattedRequestMinuteTime);
                                preparedStatement.setString(3, logRecordAggregateResult.getService());
                                preparedStatement.setString(4, logRecordAggregateResult.getMethod());
                                preparedStatement.setInt(5, logRecordAggregateResult.getCount());
                                preparedStatement.setString(6, JacksonUtil.toJson(logRecordAggregateResult.getCodeToCountMap()));
                                preparedStatement.setLong(7, logRecordAggregateResult.getTotalTime());
                                preparedStatement.setDouble(8, logRecordAggregateResult.getAvgTime());
                            }
                        },
                        JdbcExecutionOptions.builder().withBatchIntervalMs(200)             // optional: default = 0, meaning no time-based execution is done
                                .withBatchSize(1000)                  // optional: default = 5000 values
                                .withMaxRetries(5)                    // optional: default = 3
                                .build(),
                        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                .withUrl("jdbc:mysql://127.0.0.1:3306/hao_db_2")
                                .withUsername("root")
                                .withPassword("root_123")
                                .withDriverName("com.mysql.jdbc.Driver")
                                .withConnectionCheckTimeoutSeconds(30)
                                .build()));

        KeySelector<LogRecordAggregateResult, Tuple3<Long, String, String>> keySelector2 = new KeySelector<LogRecordAggregateResult, Tuple3<Long, String, String>>() {
            @Override
            public Tuple3<Long, String, String> getKey(LogRecordAggregateResult value) throws Exception {
                Tuple3<Long, String, String> key = new Tuple3<>(value.getRequestMinuteTimeStamp(), value.getService(), value.getMethod());
                //log.info("KeySelector2 key: {}", key);
                return key;
            }
        };

        SingleOutputStreamOperator<LogRecordAggregateResult> reducedDataStream = aggregateDataStream
                .keyBy(keySelector2)
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
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
                }, new ProcessWindowFunction<LogRecordAggregateResult, LogRecordAggregateResult, Tuple3<Long, String, String>, TimeWindow>() {
                    @Override
                    public void process(Tuple3<Long, String, String> key, ProcessWindowFunction<LogRecordAggregateResult, LogRecordAggregateResult, Tuple3<Long, String, String>, TimeWindow>.Context context, Iterable<LogRecordAggregateResult> elements, Collector<LogRecordAggregateResult> out) throws Exception {
                        LogRecordAggregateResult logRecordAggregateResult = elements.iterator().next();
                        out.collect(logRecordAggregateResult);
                        String formatted = String.format("reducedDataStream, key: %s, logRecordAggregateResult: %s", key.toString(), logRecordAggregateResult);
                        System.out.println(formatted);
                    }
                });

        //reducedDataStream.addSink(new PrintSinkFunction<>("reducedDataStream"));

        String insertSql2 = "insert into log_record_final " +
                "(requestMinuteTimeStamp, requestMinuteTime, service, method, count, codeToCountMap, totalTime, avgTime)" +
                " values (?, ?, ?, ?, ?, ?, ?, ?)";
        reducedDataStream.addSink(
                JdbcSink.sink(
                        insertSql2,
                        new JdbcStatementBuilder<LogRecordAggregateResult>() {
                            @Override
                            public void accept(PreparedStatement preparedStatement, LogRecordAggregateResult logRecordAggregateResult) throws SQLException {
                                Date requestMinuteTime = logRecordAggregateResult.getRequestMinuteTime();
                                String formattedRequestMinuteTime = DateFormatUtils.format(requestMinuteTime, "yyyy-MM-dd HH:mm:ss");

                                preparedStatement.setLong(1, logRecordAggregateResult.getRequestMinuteTimeStamp());
                                preparedStatement.setString(2, formattedRequestMinuteTime);
                                preparedStatement.setString(3, logRecordAggregateResult.getService());
                                preparedStatement.setString(4, logRecordAggregateResult.getMethod());
                                preparedStatement.setInt(5, logRecordAggregateResult.getCount());
                                preparedStatement.setString(6, JacksonUtil.toJson(logRecordAggregateResult.getCodeToCountMap()));
                                preparedStatement.setLong(7, logRecordAggregateResult.getTotalTime());

                                // 计算平均耗时
                                double avgTime = logRecordAggregateResult.getTotalTime() * 1D / logRecordAggregateResult.getCount();
                                preparedStatement.setDouble(8, avgTime);
                            }
                        },
                        JdbcExecutionOptions.builder().withBatchIntervalMs(200)             // optional: default = 0, meaning no time-based execution is done
                                .withBatchSize(1000)                  // optional: default = 5000 values
                                .withMaxRetries(5)                    // optional: default = 3
                                .build(),
                        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                .withUrl("jdbc:mysql://127.0.0.1:3306/hao_db_2")
                                .withUsername("root")
                                .withPassword("root_123")
                                .withDriverName("com.mysql.jdbc.Driver")
                                .withConnectionCheckTimeoutSeconds(30)
                                .build()));

        // 执行程序
        env.execute("Flink Socket Source Example");
    }

}
