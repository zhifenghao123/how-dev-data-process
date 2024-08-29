package com.howdev.flinkdev.transaction.sink.Influxdb.v1;

import org.apache.flink.util.Preconditions;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class InfluxDbConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_BATCH_ACTIONS = 2000;
    private static final int DEFAULT_FLUSH_DURATION = 100;

    private String url;
    private String username;
    private String password;
    private String database;
    private int batchActions;
    private int flushDuration;
    private TimeUnit flushDurationTimeUnit;
    private boolean enableGzip;
    private boolean createDatabase;

    public InfluxDbConfig(InfluxDbConfig.Builder builder) {
        Preconditions.checkArgument(builder != null, "InfluxDBConfig builder can not be null");

        this.url = Preconditions.checkNotNull(builder.getUrl(), "host can not be null");
        this.username = Preconditions.checkNotNull(builder.getUsername(), "username can not be null");
        this.password = Preconditions.checkNotNull(builder.getPassword(), "password can not be null");
        this.database = Preconditions.checkNotNull(builder.getDatabase(), "database name can not be null");

        this.batchActions = builder.getBatchActions();
        this.flushDuration = builder.getFlushDuration();
        this.flushDurationTimeUnit = builder.getFlushDurationTimeUnit();
        this.enableGzip = builder.isEnableGzip();
        this.createDatabase = builder.isCreateDatabase();
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public int getBatchActions() {
        return batchActions;
    }

    public int getFlushDuration() {
        return flushDuration;
    }

    public TimeUnit getFlushDurationTimeUnit() {
        return flushDurationTimeUnit;
    }

    public boolean isEnableGzip() {
        return enableGzip;
    }

    public boolean isCreateDatabase() { return createDatabase; }

    /**
     * Creates a new {@link InfluxDbConfig.Builder} instance.
     * <p/>
     * This is a convenience method for {@code new InfluxDBConfig.Builder()}.
     *
     * @param url      the url to connect to
     * @param username the username which is used to authorize against the influxDB instance
     * @param password the password for the username which is used to authorize against the influxDB
     *                 instance
     * @param database the name of the database to write
     * @return the new InfluxDBConfig builder.
     */
    public static Builder builder(String url, String username, String password, String database) {
        return new Builder(url, username, password, database);
    }

    /**
     * A builder used to create a build an instance of a InfluxDBConfig.
     */
    public static class Builder {
        private String url;
        private String username;
        private String password;
        private String database;
        private int batchActions = DEFAULT_BATCH_ACTIONS;
        private int flushDuration = DEFAULT_FLUSH_DURATION;
        private TimeUnit flushDurationTimeUnit = TimeUnit.MILLISECONDS;
        private boolean enableGzip = false;
        private boolean createDatabase = false;

        /**
         * Creates a builder
         *
         * @param url      the url to connect to
         * @param username the username which is used to authorize against the influxDB instance
         * @param password the password for the username which is used to authorize against the influxDB
         *                 instance
         * @param database the name of the database to write
         */
        public Builder(String url, String username, String password, String database) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.database = database;
        }

        /**
         * Sets url.
         *
         * @param url the url to connect to
         * @return this Builder to use it fluent
         */
        public InfluxDbConfig.Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Sets username.
         *
         * @param username the username which is used to authorize against the influxDB instance
         * @return this Builder to use it fluent
         */
        public InfluxDbConfig.Builder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * Sets password.
         *
         * @param password the password for the username which is used to authorize against the influxDB
         *                 instance
         * @return this Builder to use it fluent
         */
        public InfluxDbConfig.Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets database name.
         *
         * @param database the name of the database to write
         * @return this Builder to use it fluent
         */
        public InfluxDbConfig.Builder database(String database) {
            this.database = database;
            return this;
        }

        /**
         * Sets when to flush a new bulk request based on the number of batch actions currently added.
         * Defaults to <tt>DEFAULT_BATCH_ACTIONS</tt>. Can be set to <tt>-1</tt> to disable it.
         *
         * @param batchActions number of Points written after which a write must happen.
         * @return this Builder to use it fluent
         */
        public InfluxDbConfig.Builder batchActions(int batchActions) {
            this.batchActions = batchActions;
            return this;
        }

        /**
         * Sets a flush interval flushing *any* bulk actions pending if the interval passes.
         *
         * @param flushDuration         the flush duration
         * @param flushDurationTimeUnit the TimeUnit of the flush duration
         * @return this Builder to use it fluent
         */
        public Builder flushDuration(int flushDuration, TimeUnit flushDurationTimeUnit) {
            this.flushDuration = flushDuration;
            this.flushDurationTimeUnit = flushDurationTimeUnit;
            return this;
        }

        /**
         * Enable Gzip compress for http request body.
         *
         * @param enableGzip the enableGzip value
         * @return this Builder to use it fluent
         */
        public InfluxDbConfig.Builder enableGzip(boolean enableGzip) {
            this.enableGzip = enableGzip;
            return this;
        }

        /**
         * Make InfluxDb sink create new database
         *
         * @param createDatabase createDatabase switch value
         * @return this Builder to use it fluent
         */
        public InfluxDbConfig.Builder createDatabase(boolean createDatabase) {
            this.createDatabase = createDatabase;
            return this;
        }

        /**
         * Builds InfluxDBConfig.
         *
         * @return the InfluxDBConfig instance.
         */
        public InfluxDbConfig build() {
            return new InfluxDbConfig(this);
        }


        public String getUrl() {
            return url;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getDatabase() {
            return database;
        }

        public int getBatchActions() {
            return batchActions;
        }

        public int getFlushDuration() {
            return flushDuration;
        }

        public TimeUnit getFlushDurationTimeUnit() {
            return flushDurationTimeUnit;
        }

        public boolean isEnableGzip() {
            return enableGzip;
        }

        public boolean isCreateDatabase() { return createDatabase; }
    }
}