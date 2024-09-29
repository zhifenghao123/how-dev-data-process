package com.howdev.flinkdev.log.main;

import com.howdev.common.util.JacksonUtil;
import com.howdev.flinkdev.log.watermark.MyWatermarkStrategy;
import com.howdev.flinkdev.log.biz.domain.LogRecord;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class LogComplexAggregate2 {
    public static void main(String[] args) throws Exception {
        // 创建流执行环境
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 创建带有WebUI的本地流执行环境
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(4);


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

                int randomNum = (int) (Math.random() * 4) + 1;
                logRecord.setRandomNum(randomNum);

                //System.out.println(JacksonUtil.toJson(logRecord));

                // debug，数据写入到mysql中
                saveToMysql(logRecord);

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

        KeySelector<LogRecord, Tuple2<String, Integer>> keySelector = new KeySelector<LogRecord, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> getKey(LogRecord value) throws Exception {
                Tuple2<String, Integer> key = new Tuple2<>(value.getService(), value.getRandomNum());
                log.info("KeySelector key: {}", key);
                return key;
            }
        };

        SingleOutputStreamOperator<LogStatResult> aggregatedDataStream = assignTimestampsAndWatermarkDataStream
                .keyBy(keySelector)
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .aggregate(new AggregateFunction<LogRecord, LogStatResult, LogStatResult>() {
                    @Override
                    public LogStatResult createAccumulator() {
                        return new LogStatResult();
                    }

                    @Override
                    public LogStatResult add(LogRecord value, LogStatResult accumulator) {
                        long requestTimeStamp = value.getRequestTimeStamp();
                        // 将13位的毫秒级时间戳requestTimeStamp转为10位的秒级时间戳
                        // 将毫秒级时间戳转换为秒级时间戳
                        long secondTimeStamp = requestTimeStamp / 1000;

                        if (accumulator.getDimension() == null) {
                            LogStatDimension dimension = new LogStatDimension();
                            dimension.setService(value.getService());
                            accumulator.setDimension(dimension);

                            LogStatMetric metric = new LogStatMetric();
                            metric.initCount(secondTimeStamp, 1L);
                            metric.setTotalCost(value.getCost());

                            accumulator.setMetric(metric);
                            accumulator.initDetailResultMap(value, secondTimeStamp);
                        } else {
                            LogStatMetric accumulatorMetric = accumulator.getMetric();
                            accumulatorMetric.addCount(secondTimeStamp, 1L);
                            accumulatorMetric.setTotalCost(accumulatorMetric.getTotalCost() + value.getCost());

                            accumulator.setMetric(accumulatorMetric);
                            accumulator.addDetailResult(value, secondTimeStamp);
                        }

                        return accumulator;
                    }

                    @Override
                    public LogStatResult getResult(LogStatResult accumulator) {
                        accumulator.calculateAllDetailResult();

                        LogStatMetric accumulatorMetric = accumulator.getMetric();
                        accumulatorMetric.calculateAndSetAvgCost();
                        accumulatorMetric.calculateAndSetMaxQps();
                        return accumulator;
                    }

                    @Override
                    public LogStatResult merge(LogStatResult a, LogStatResult b) {
                        return null;
                    }
                }, new ProcessWindowFunction<LogStatResult, LogStatResult, Tuple2<String, Integer>, TimeWindow>() {
                    @Override
                    public void process(Tuple2<String, Integer> key, ProcessWindowFunction<LogStatResult, LogStatResult, Tuple2<String, Integer>, TimeWindow>.Context context, Iterable<LogStatResult> elements, Collector<LogStatResult> out) throws Exception {
                        long windowStartTime = context.window().getStart();
                        long windowEndTime = context.window().getEnd();

                        String formattedWindowStart = DateFormatUtils.format(windowStartTime, "yyyy-MM-dd HH:mm:ss");
                        String formattedWindowEnd = DateFormatUtils.format(windowEndTime, "yyyy-MM-dd HH:mm:ss");


                        String printFormat =String.format("key: %s, windowStart: %s, windowEnd: %s", key.toString(), formattedWindowStart, formattedWindowEnd);

                        System.out.println(printFormat);

                        LogStatResult logStatResult = elements.iterator().next();
                        logStatResult.setRequestTime(formattedWindowStart);
                        logStatResult.setRequestTimeStamp(windowStartTime);

                        out.collect(logStatResult);
                    }
                });


        SingleOutputStreamOperator<LogMethodAggResult> methodAggResultDataStream = aggregatedDataStream.flatMap(new FlatMapFunction<LogStatResult, LogMethodAggResult>() {
            @Override
            public void flatMap(LogStatResult value, Collector<LogMethodAggResult> out) throws Exception {
                long requestTimeStamp = value.getRequestTimeStamp();
                String requestTime = value.getRequestTime();

                LogStatDimension dimension = value.getDimension();
                String service = dimension.getService();

                Map<LogStatDetailDimension, LogStatMetric> detailResultMap = value.getDetailResultMap();
                for (LogStatDetailDimension detailDimension : detailResultMap.keySet()) {
                    String method = detailDimension.getMethod();

                    LogStatMetric logStatMetric = detailResultMap.get(detailDimension);
                    Long totalCount = logStatMetric.getTotalCount();
                    Long maxQps = logStatMetric.getMaxQps();
                    Double avgCost = logStatMetric.getAvgCost();

                    LogMethodAggResult logMethodAggResult = new LogMethodAggResult();
                    logMethodAggResult.setRequestTime(requestTime);
                    logMethodAggResult.setRequestTimeStamp(requestTimeStamp);
                    logMethodAggResult.setService(service);
                    logMethodAggResult.setMethod(method);
                    logMethodAggResult.setTotalCount(totalCount);
                    logMethodAggResult.setAvgCost(avgCost);
                    logMethodAggResult.setMaxQps(maxQps);

                    out.collect(logMethodAggResult);
                }
            }
        });

        String insertSql = "INSERT INTO log_method_agg (requestTime, requestTimeStamp, service, method, totalCount, maxQps, avgCost) VALUES (?, ?, ?, ?, ?, ?, ?)";
        methodAggResultDataStream.addSink(
                JdbcSink.sink(
                        insertSql,
                        new JdbcStatementBuilder<LogMethodAggResult>() {
                            @Override
                            public void accept(PreparedStatement preparedStatement, LogMethodAggResult logMethodAggResult) throws SQLException {
                                //Date requestMinuteTime = LogMethodAggResult.getRequestTime();
                                //String formattedRequestMinuteTime = DateFormatUtils.format(requestMinuteTime, "yyyy-MM-dd HH:mm:ss");

                                preparedStatement.setString(1, logMethodAggResult.getRequestTime());
                                preparedStatement.setLong(2, logMethodAggResult.getRequestTimeStamp());
                                preparedStatement.setString(3, logMethodAggResult.getService());
                                preparedStatement.setString(4, logMethodAggResult.getMethod());
                                preparedStatement.setLong(5, logMethodAggResult.getTotalCount());
                                preparedStatement.setLong(6, logMethodAggResult.getMaxQps() == null ? 0L : logMethodAggResult.getMaxQps());
                                preparedStatement.setDouble(7, logMethodAggResult.getAvgCost());
                            }
                        },
                        JdbcExecutionOptions.builder().withBatchIntervalMs(200)             // optional: default = 0, meaning no time-based execution is done
                                .withBatchSize(2)                  // optional: default = 5000 values
                                .withMaxRetries(2)                    // optional: default = 3
                                .build(),
                        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                .withUrl("jdbc:mysql://127.0.0.1:3306/hao_db3")
                                .withUsername("root")
                                .withPassword("root_123")
                                .withDriverName("com.mysql.jdbc.Driver")
                                .withConnectionCheckTimeoutSeconds(30)
                                .build()));

        env.execute();

    }












    @Data
    static class LogStatResult {
        String requestTime;
        long requestTimeStamp;

        LogStatDimension dimension;
        LogStatMetric metric;
        Map<LogStatDetailDimension, LogStatMetric> detailResultMap = new HashMap<>();

        public void initDetailResultMap (LogRecord logRecord, long secondTimeStamp) {
            LogStatDetailDimension detailDimension = new LogStatDetailDimension();
            detailDimension.setMethod(logRecord.getMethod());

            LogStatMetric metric = new LogStatMetric();
            metric.initCount(secondTimeStamp, 1L);
            metric.setTotalCost(logRecord.getCost());

            detailResultMap.put(detailDimension, metric);
        }

        public void addDetailResult(LogRecord logRecord, long secondTimeStamp) {
            LogStatDetailDimension detailDimension = new LogStatDetailDimension();
            detailDimension.setMethod(logRecord.getMethod());

            LogStatMetric logStatMetric = detailResultMap.get(detailDimension);

            if (logStatMetric == null) {
                initDetailResultMap(logRecord, secondTimeStamp);
            } else {
                logStatMetric.addCount(secondTimeStamp, 1L);
                logStatMetric.setTotalCost(logStatMetric.getTotalCost() + logRecord.getCost());
            }

        }

        public void calculateAllDetailResult() {
            for (LogStatMetric logStatMetric : detailResultMap.values()) {
                logStatMetric.calculateAndSetAvgCost();
                logStatMetric.calculateAndSetMaxQps();
            }
        }
    }


    @Data
    static class LogStatMetric {
        Long totalCount;
        Long totalCost;
        Double avgCost;

        Map<Long, Long> secondTimestampToCountMap;
        Long maxQps;

        public void initCount(Long secondTimestamp, Long count) {
            Map<Long, Long> secondTimestampToCountMap = new HashMap<>();
            secondTimestampToCountMap.put(secondTimestamp, count);

            this.secondTimestampToCountMap = secondTimestampToCountMap;
            this.totalCount = count;
        }

        public void addCount(Long secondTimestamp, Long count) {
            long currentSecondCountExistCount = secondTimestampToCountMap.get(secondTimestamp) == null ? 0L : secondTimestampToCountMap.get(secondTimestamp);
            secondTimestampToCountMap.put(secondTimestamp, currentSecondCountExistCount + 1);

            this.totalCount = this.totalCount + count;
        }

        public void calculateAndSetAvgCost() {
            // 计算平均耗时
            double calAvgCost = 0.0;

            try {
                // 计算平均耗时
                calAvgCost = totalCost / totalCount;

            } catch (Exception e) {
                System.out.println("calculate avgTotalCost and avgFrameworkCost error");
            }
            this.avgCost = calAvgCost;
        }

        public void calculateAndSetMaxQps() {
            // 计算本分钟内最大QPS
            long maxQps = 0;
            for (Map.Entry<Long, Long> entry : secondTimestampToCountMap.entrySet()) {
                if (entry.getValue() > maxQps) {
                    maxQps = entry.getValue();
                }
            }
        }

    }

    @Data
    static class LogStatDimension {
        String service;
    }

    @Data
    static class LogStatDetailDimension {
        String method;
    }

    @Data
    static class LogServiceAggResult {
        String requestTime;
        long requestTimeStamp;
        String service;
        String method;

        Long totalCount;
        Long maxQps;

        Double avgCost;
    }
    @Data
    static class LogMethodAggResult {
        String requestTime;
        long requestTimeStamp;
        String service;
        String method;

        Long totalCount;
        Long maxQps;

        Double avgCost;
    }

    private static void saveToMysql(LogRecord logRecord) {
        String DB_URL = "jdbc:mysql://localhost:3306/hao_db3";
        String USER = "root";
        String PASS = "root_123";
        String insertSql = "INSERT INTO log_record_random (requestId, requestTimeStamp, randomNum, service, method, returnCode, cost) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement preparedStatement = conn.prepareStatement(insertSql)) {

            preparedStatement.setString(1, logRecord.getRequestId());
            preparedStatement.setLong(2, logRecord.getRequestTimeStamp());
            preparedStatement.setInt(3, logRecord.getRandomNum());
            preparedStatement.setString(4, logRecord.getService());
            preparedStatement.setString(5, logRecord.getMethod());
            preparedStatement.setString(6, logRecord.getReturnCode());
            preparedStatement.setLong(7, logRecord.getCost());

            int affectedRows = preparedStatement.executeUpdate();
            System.out.println("插入成功，影响了 " + affectedRows + " 行。");

        } catch (SQLException e) {
            System.err.println("数据库操作失败！");
            e.printStackTrace();
        }

    }

}
