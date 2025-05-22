package com.howdev.flinkdev.flink.sink.Influxdb.v2;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.write.Point;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.util.Preconditions;

public class InfluxDbV2Sink extends RichSinkFunction<Point> {

    private final InfluxDbV2Config influxDBV2Config;

    private transient InfluxDBClient influxDBClient;

    private transient WriteApi writeApi;

    public InfluxDbV2Sink(InfluxDbV2Config influxDBV2Config) {
        this.influxDBV2Config = Preconditions.checkNotNull(influxDBV2Config, "InfluxDB client config should not be null");
    }


    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        String url = influxDBV2Config.getUrl();
        String token = influxDBV2Config.getToken();
        String org = influxDBV2Config.getOrg();
        String bucketName = influxDBV2Config.getBucket();

        influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucketName);

        if (influxDBV2Config.isEnableGzip()) {
            influxDBClient.enableGzip();
        }
        writeApi = influxDBClient.makeWriteApi();
    }


    @Override
    public void invoke(Point dataPoint, Context context) throws Exception {

        // 写入数据
        // 注意：它可能依赖于内部优化或让你使用Flux查询语言来更高效地处理批量写入。
        writeApi.writePoint(dataPoint);
    }

    @Override
    public void close() {
        influxDBClient.close();
    }
}