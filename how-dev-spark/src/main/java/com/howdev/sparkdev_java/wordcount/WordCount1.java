package com.howdev.sparkdev_java.wordcount;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

public class WordCount1 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf();
        conf.setMaster("local[4]");
        conf.setAppName(WordCount1.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);

        JavaRDD<String> rawRdd = jsc.textFile("how-dev-spark/test-datasets/sparkdev_java/wordcount.txt");

        JavaRDD<String> wordRdd = rawRdd.flatMap(line -> Arrays.asList(line.split(" ")).iterator());

        JavaPairRDD<String, Iterable<String>> wordGroupRdd = wordRdd.groupBy(word -> word);

        JavaPairRDD<String, Integer> wordCountRdd = wordGroupRdd.mapValues(iter -> {
            int count = 0;
            for (String word : iter) {
                count++;
            }
            return count;
        });

        // 按照count降序排序
        JavaPairRDD<Integer, String> countWordRdd = wordCountRdd
                .mapToPair(tuple -> new Tuple2<>(tuple._2(), tuple._1()))
                .sortByKey(false);  // false表示降序

        countWordRdd
                .collect().forEach(System.out::println);

        jsc.close();
    }
}
