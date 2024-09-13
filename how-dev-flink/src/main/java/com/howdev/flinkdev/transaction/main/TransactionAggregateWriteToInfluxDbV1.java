package com.howdev.flinkdev.transaction.main;

import com.howdev.flinkdev.transaction.sink.Influxdb.v1.InfluxDbV1Config;
import com.howdev.flinkdev.transaction.sink.Influxdb.v1.InfluxDbV1Sink;
import com.howdev.mock.dto.Transaction;
import com.howdev.mock.stream.MockStreamSource;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.CollectionUtil;
import org.influxdb.dto.Point;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TransactionAggregateWriteToInfluxDbV1 {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setString("rest.port","9001");

        // StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 创建带有WebUI的本地流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        DataStream<Transaction> streamSource = env.addSource(new MockStreamSource.MyParallelTransactionStreamSource());
        DataStream<Point> influxDBPointDataStream = streamSource.map(new MapFunction<Transaction, Point>() {
            @Override
            public Point map(Transaction value) {
                Date occurredTime = value.getOccurredTime();
                long timestampMillis = occurredTime.getTime();

                Map<String, String> tags = new HashMap<>();
                tags.put("userId", String.valueOf(value.getUserId()));
                tags.put("type", value.getType());
                tags.put("location", value.getOccurredLocation());


                Map<String, Object> fields = new HashMap<>();
                fields.put("amount", value.getAmount());

                Point.Builder builder = Point.measurement("transaction_log")
                        .time(timestampMillis, TimeUnit.MILLISECONDS);

                if (!CollectionUtil.isNullOrEmpty(fields)) {
                    builder.fields(fields);
                }

                if (!CollectionUtil.isNullOrEmpty(tags)) {
                    builder.tag(tags);
                }

                Point dataPoint = builder.build();

                return dataPoint;

            }
        });
        InfluxDbV1Config influxDbV1Config = getInfluxDbConfig();

        InfluxDbV1Sink influxDbV1Sink = new InfluxDbV1Sink(influxDbV1Config);
        influxDBPointDataStream.addSink(influxDbV1Sink);

        String jobName = TransactionAggregateWriteToInfluxDbV1.class.getSimpleName();
        env.execute(jobName);

    }

    private static InfluxDbV1Config getInfluxDbConfig() {
        String influxDBUrl = "http://127.0.0.1:8086";
        String influxDBUsername = "root";
        String influxDBPassword = "root_123";
        String influxDBDatabase = "hao_test2";
        InfluxDbV1Config.Builder influxDBConfigBuilder = new InfluxDbV1Config.Builder(influxDBUrl, influxDBUsername, influxDBPassword, influxDBDatabase);
        return influxDBConfigBuilder.build();
    }
}
