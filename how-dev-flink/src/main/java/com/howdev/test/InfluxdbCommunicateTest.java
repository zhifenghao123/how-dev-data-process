package com.howdev.test;

import com.influxdb.client.BucketsApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class InfluxdbCommunicateTest {
    public static void main(String[] args) {
        test2();
    }

    public static void test1() {
        // InfluxDB 1.x 连接参数
//        String url = "http://localhost:8086";
//        String username = "root";
//        String password = "root_123";
//        String database = "hao_test2";

        // InfluxDB 2.x 连接参数
        String url = "http://localhost:8086";
        String token = "bkWXRkEVhLnkkSdiZ2uze15PqvkXXB9qYpjKpPCNseebhep8D4NQasRzeau2vyROI48GQv6Kj3VrUdIGZLDIPg==";
        String org = "hao";
        String bucket = "hao_test2";

        // 创建InfluxDB客户端
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);

        // 创建写入器
        WriteApi writeApi = influxDBClient.makeWriteApi();

        // 创建Point并写入数据
        Point point = Point.measurement("measurement_1")
                .addTag("tag_key", "tag_value3")
                .addField("field_key", "field_value3")
                .time(System.currentTimeMillis(), WritePrecision.MS);

        // 写入数据
        writeApi.writePoint(point);

        // 关闭客户端
        influxDBClient.close();
    }

    public static void test2() {

        // InfluxDB 2.x 连接参数
        String url = "http://localhost:8086";
        String token = "bkWXRkEVhLnkkSdiZ2uze15PqvkXXB9qYpjKpPCNseebhep8D4NQasRzeau2vyROI48GQv6Kj3VrUdIGZLDIPg==";
        String org = "hao";
        String bucketName = "hao_test2";

        // 创建InfluxDB客户端
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucketName);

        // 检查bucket是否存在
        BucketsApi bucketsApi = influxDBClient.getBucketsApi();
        Bucket existedBucket = bucketsApi.findBucketByName(bucketName);

        if (Objects.isNull(existedBucket)) {
            // 如果bucket不存在，则创建它
            Bucket createdNewBucket = bucketsApi.createBucket(bucketName, org);
            System.out.println("Bucket " + bucketName + " created.");
        } else {
            System.out.println("Bucket " + bucketName + " already exists.");
        }

        // 创建写入器
        WriteApi writeApi = influxDBClient.makeWriteApi();
        Map<String, String> tags = new HashMap<>();
        tags.put("tag_key1", "tag_value3");
        tags.put("tag_key2", "tag_value3");

        Map<String, Object> fields = new HashMap<>();
        fields.put("field_key1", "field_value3");
        fields.put("field_key2", "field_value3");
        Point point = Point.measurement("measurement_08081304")
                        .addTags(tags)
                        .addFields(fields)
                        .time(System.currentTimeMillis(), WritePrecision.MS);

        writeApi.writePoint(point);

        // 关闭客户端
        influxDBClient.close();
    }
}