package com.howdev.sparklearn_java.sparkcore.rdd.builder;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class Spark_Rdd_Builder_01 {
    public static void main(String[] args) {
        // TODO 构建Spark的运行环境

        // 缺少conf会报错 SparkException：A master URL must be set in your configuration
        // final JavaSparkContext jsc = new JavaSparkContext();

        SparkConf conf = new SparkConf();
        conf.setMaster("local");

        // 对SparkConf不设置appName会报错 SparkException: An application name must be set in your configuration
        conf.setAppName(Spark_Rdd_Builder_01.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);

        // TODO 释放资源
        jsc.close();
    }
}
