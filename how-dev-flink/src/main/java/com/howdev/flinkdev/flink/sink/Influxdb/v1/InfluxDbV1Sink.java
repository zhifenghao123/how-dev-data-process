package com.howdev.flinkdev.flink.sink.Influxdb.v1;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.util.Preconditions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

public class InfluxDbV1Sink extends RichSinkFunction<Point> {

    private transient InfluxDB influxDBClient;
    private final InfluxDbV1Config influxDBV1Config;

    /**
     * Creates a new {@link InfluxDbV1Sink} that connects to the InfluxDB server.
     *
     * @param influxDBV1Config The configuration of {@link InfluxDbV1Config}
     */
    public InfluxDbV1Sink(InfluxDbV1Config influxDBV1Config) {
        this.influxDBV1Config = Preconditions.checkNotNull(influxDBV1Config, "InfluxDB client config should not be null");
    }

    /**
     * Initializes the connection to InfluxDB by either cluster or sentinels or single server.
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        String influxDbUrl = influxDBV1Config.getUrl();
        String influxDbUsername = influxDBV1Config.getUsername();
        String influxDbPassword = influxDBV1Config.getPassword();
        influxDBClient = InfluxDBFactory.connect(influxDbUrl, influxDbUsername, influxDbPassword);


        String influxDbDatabase = influxDBV1Config.getDatabase();
        if (!influxDBClient.databaseExists(influxDbDatabase)) {
            throw new RuntimeException("This " + influxDbDatabase + " database does not exist!");
        }

        influxDBClient.setDatabase(influxDbDatabase);

        if (influxDBV1Config.getBatchActions() > 0) {
            influxDBClient.enableBatch(influxDBV1Config.getBatchActions(), influxDBV1Config.getFlushDuration(), influxDBV1Config.getFlushDurationTimeUnit());
        }

        if (influxDBV1Config.isEnableGzip()) {
            influxDBClient.enableGzip();
        }
    }

    /**
     * Called when new data arrives to the sink, and forwards it to InfluxDB.
     *
     * @param dataPoint {@link Point}
     */
    @Override
    public void invoke(Point dataPoint, Context context) throws Exception {

        influxDBClient.write(dataPoint);
    }

    @Override
    public void close() {
        if (influxDBClient.isBatchEnabled()) {
            influxDBClient.disableBatch();
        }
        influxDBClient.close();
    }
}