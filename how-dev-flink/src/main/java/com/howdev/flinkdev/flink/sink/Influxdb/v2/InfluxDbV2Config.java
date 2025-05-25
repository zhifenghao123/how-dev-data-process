package com.howdev.flinkdev.flink.sink.Influxdb.v2;

import org.apache.flink.util.Preconditions;

import java.io.Serializable;

public class InfluxDbV2Config implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_BATCH_ACTIONS = 2000;
    private static final int DEFAULT_FLUSH_DURATION = 100;

    private String url;
    private String token;
    private String org;
    private String bucket;
    private boolean enableGzip;

    public InfluxDbV2Config(Builder builder) {
        Preconditions.checkArgument(builder != null, "InfluxDBConfig builder can not be null");

        this.url = Preconditions.checkNotNull(builder.getUrl(), "host can not be null");
        this.token = Preconditions.checkNotNull(builder.getUsername(), "token can not be null");
        this.org = Preconditions.checkNotNull(builder.getPassword(), "org can not be null");
        this.bucket = Preconditions.checkNotNull(builder.getDatabase(), "bucket name can not be null");

        this.enableGzip = builder.isEnableGzip();
    }

    public String getUrl() {
        return url;
    }

    public String getToken() {
        return token;
    }

    public String getOrg() {
        return org;
    }

    public String getBucket() {
        return bucket;
    }

    public boolean isEnableGzip() {
        return enableGzip;
    }

    /**
     * Creates a new {@link Builder} instance.
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
        private boolean enableGzip = false;

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
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Sets username.
         *
         * @param username the username which is used to authorize against the influxDB instance
         * @return this Builder to use it fluent
         */
        public Builder username(String username) {
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
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets database name.
         *
         * @param database the name of the database to write
         * @return this Builder to use it fluent
         */
        public Builder database(String database) {
            this.database = database;
            return this;
        }


        /**
         * Enable Gzip compress for http request body.
         *
         * @param enableGzip the enableGzip value
         * @return this Builder to use it fluent
         */
        public Builder enableGzip(boolean enableGzip) {
            this.enableGzip = enableGzip;
            return this;
        }

        /**
         * Builds InfluxDBConfig.
         *
         * @return the InfluxDBConfig instance.
         */
        public InfluxDbV2Config build() {
            return new InfluxDbV2Config(this);
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

        public boolean isEnableGzip() {
            return enableGzip;
        }
    }
}