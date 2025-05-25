package com.howdev.sparklearn_java.sparkcore.rdd.builder;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

public class Spark_Rdd_Builder_03_Disk {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf();
        conf.setMaster("local");
        conf.setAppName(Spark_Rdd_Builder_03_Disk.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);
        // 利用SparkContext环境对象对接磁盘数据文件，构建RDD
        JavaRDD<String> rdd = jsc.textFile("how-dev-spark/test-datasets/sparklearn_java/hello/in/hello.txt");


        List<String> collectData = rdd.collect();
        collectData.forEach(System.out::println);

        jsc.close();
    }
}
