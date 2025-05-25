package com.howdev.sparklearn_java.sparkcore.rdd.operator.transformation;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import scala.Tuple1$;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Spark_Rdd_Transformation3_KV {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf();
        conf.setMaster("local");
        conf.setAppName(Spark_Rdd_Transformation3_KV.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);

        List<Tuple2<String, Integer>> kvData = Arrays.asList(
                new Tuple2<>("a", 1),
                new Tuple2<>("b", 2),
                new Tuple2<>("a", 3),
                new Tuple2<>("d", 4));

        jsc.parallelizePairs(kvData, 3)
                // mapValues只对value进行操作，key不变
                .mapValues(v1 -> {
                    return (Integer) v1 * 2;
                })
                .collect()
                .forEach(System.out::println);

        jsc.parallelizePairs(kvData, 3)
                // 对于相同的key，将对应的value放在一起，而不是将完整的key-value放在一起
                .groupByKey()
                .collect()
                .forEach(System.out::println);

        jsc.close();
    }
}

