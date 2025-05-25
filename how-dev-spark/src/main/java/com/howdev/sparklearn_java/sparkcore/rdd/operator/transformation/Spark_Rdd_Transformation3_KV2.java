package com.howdev.sparklearn_java.sparkcore.rdd.operator.transformation;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

public class Spark_Rdd_Transformation3_KV2 {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf();
        conf.setMaster("local");
        conf.setAppName(Spark_Rdd_Transformation3_KV2.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);

        List<Tuple2<String, Integer>> kvData = Arrays.asList(
                new Tuple2<>("a", 1),
                new Tuple2<>("b", 2),
                new Tuple2<>("a", 3),
                new Tuple2<>("d", 4));

        JavaRDD<Tuple2<String, Integer>> rawRdd = jsc.parallelize(kvData, 3);

        JavaPairRDD<String, Integer> mapRdd = rawRdd.mapToPair(v -> v);
        JavaPairRDD<String, Integer> reduceSumByKey = mapRdd
                // reduceByKey，将KV类型的数据，根据K对V进行reduce操作（将多个值聚合成一个值）
                // 聚合计算的基本思想是：两两计算，再将计算结果和下一个值两两进行聚合计算
                .reduceByKey(Integer::sum);
        reduceSumByKey.collect().forEach(System.out::println);

        jsc.close();
    }
}

