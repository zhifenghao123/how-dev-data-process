package com.howdev.sparklearn_java.sparkcore.rdd.builder;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

public class Spark_Rdd_Builder_02_Memory {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf();
        conf.setMaster("local");
        conf.setAppName(Spark_Rdd_Builder_02_Memory.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);

        final List<String> list = Arrays.asList("hello", "world", "spark", "hadoop", "java", "python", "scala", "hive",
                "flink", "spark", "hadoop", "java", "python", "scala", "hive", "flink");

        JavaRDD<String> rdd = jsc.parallelize(list);

        // 下面这种会报错 SparkException: Task not serializable
        // 因为在 Spark 中，当你在 RDD 操作(如 foreach)中使用外部对象时，这些对象需要能够被序列化以便传输到各个 executor 节点。
        // System.out 是一个不可序列化的对象，因此导致了 NotSerializableException。
        // rdd.foreach(System.out::println);

        // 下面这种不会报错
        // 使用 lambda 表达式替代方法引用,虽然这看起来和之前类似，但有时可以避免序列化问题。
        //rdd.foreach(x -> System.out.println(x));


        // 这种方式将数据收集到 driver 端后再打印，避免了序列化问题。
        List<String> collectData = rdd.collect();
        collectData.forEach(System.out::println);

        jsc.close();
    }
}
