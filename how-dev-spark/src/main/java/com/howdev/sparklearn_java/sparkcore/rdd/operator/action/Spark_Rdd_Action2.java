package com.howdev.sparklearn_java.sparkcore.rdd.operator.action;

import com.howdev.sparklearn_java.sparkcore.rdd.operator.transformation.Spark_Rdd_Transformation;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Spark_Rdd_Action2 {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf();
        conf.setMaster("local[2]");
        conf.setAppName(Spark_Rdd_Transformation.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);

        final List<Integer> numbers = Arrays.asList(1, 2, 3, 4);

        JavaRDD<Integer> rawRdd = jsc.parallelize(numbers, 2);

        // collect() 是将数据从Executor端拉到Driver端,在Driver端进行foreach操作
        rawRdd.collect().forEach(System.out::println);

        // foreach()分布式循环， 是在Executor端进行foreach操作。 执行效率低，但是内存占用少。
        rawRdd.foreach(System.out::println);

        // foreachPartition() 是在Executor端进行foreachPartition操作。 执行效率高，但是内存占用多（一次性传递分区所有的数据）。
        rawRdd.foreachPartition(
                partitionDataList -> {
                    for (Iterator<Integer> it = partitionDataList; it.hasNext(); ) {
                        Integer partitionData = it.next();
                        System.out.println(partitionData);
                    }
                });


        jsc.close();
    }
}

