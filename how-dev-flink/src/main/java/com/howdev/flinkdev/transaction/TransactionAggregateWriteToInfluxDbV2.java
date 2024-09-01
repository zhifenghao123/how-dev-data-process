package com.howdev.flinkdev.transaction;

import com.howdev.flinkdev.transaction.sink.Influxdb.v2.InfluxDbV2Config;
import com.howdev.flinkdev.transaction.sink.Influxdb.v2.InfluxDbV2Sink;
import com.howdev.mock.dto.Transaction;
import com.howdev.mock.stream.MockStreamSource;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TransactionAggregateWriteToInfluxDbV2 {
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
                Point point = new Point("log_record");
                point.time(timestampMillis, WritePrecision.MS);
                point.addFields(fields);
                point.addTags(tags);

                return point;

            }
        });
        InfluxDbV2Config influxDbConfig = getInfluxDbConfig();
        influxDBPointDataStream.addSink(new InfluxDbV2Sink(influxDbConfig));

        String jobName = TransactionAggregateWriteToInfluxDbV2.class.getSimpleName();
        env.execute(jobName);

    }

    private static InfluxDbV2Config getInfluxDbConfig() {
        String url = "http://localhost:8086";
        String token = "bkWXRkEVhLnkkSdiZ2uze15PqvkXXB9qYpjKpPCNseebhep8D4NQasRzeau2vyROI48GQv6Kj3VrUdIGZLDIPg==";
        String org = "hao";
        String bucketName = "hao_test2";

        InfluxDbV2Config influxDbV2Config = InfluxDbV2Config
                .builder(url, token, org, bucketName)
                .build();
        return influxDbV2Config;
    }
}
