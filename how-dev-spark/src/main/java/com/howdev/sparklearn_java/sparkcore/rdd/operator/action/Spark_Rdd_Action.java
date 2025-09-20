package com.howdev.sparklearn_java.sparkcore.rdd.operator.action;

import com.howdev.sparklearn_java.sparkcore.rdd.operator.transformation.Spark_Rdd_Transformation;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Spark_Rdd_Action {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf();
        conf.setMaster("local[2]");
        conf.setAppName(Spark_Rdd_Transformation.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);

        final List<Integer> numbers = Arrays.asList(1, 2, 3, 4);

        JavaRDD<Integer> rawRdd = jsc.parallelize(numbers, 2);

        JavaRDD<Integer> mappedRdd = rawRdd
                // Spark的计算全都是在Executor端执行的
                // Spark在编写代码时，调用转换算子，并不会真正执行，只是在Driver端组合功能
                // 当前main方法也称为Driver方法，当前运行的main线程，也称为Driver线程
                // 下面的代码（组合功能的代码）是在Driver端执行的，但是算子中的逻辑代码是在Executor端执行的，并不会在Driver端调用和执行
                // Rdd中封装的计算逻辑就是转换算子中的逻辑代码
                .map(v1 -> {
                    System.out.println("map(v -> 2*v): " + v1);
                    return v1 * 2;
                });

        List<Integer> collectData = mappedRdd
                // collect方法是action算子，会触发job的执行
                // collect方法会将Executor端执行的结果数据按照分区的顺序拉取（收集）回到Driver端，并将多个结果组合成集合对象【按照分区顺序，将多个分区上的数据合并】
                // collect可能会导致多个Executor上的大量数据拉取回Driver端，会导致Driver端的内存溢出。所以生产环境慎用（例如，原始数据是从hdfs上拉取到很多的数据）
                .collect();
        // 其他的action算法：
        // count：返回RDD中元素的个数
        // take：返回RDD中前n个元素
        // first：返回RDD中第一个元素
        // countByKey：返回RDD中每个key对应的value的个数

        collectData .forEach(System.out::println);


        // 使主程序sleep，这样SparkContext不会被关闭，可以看到Spark的运行监控情况(http://localhost:4040/)
        Thread.sleep(1000000);
        jsc.close();
    }
}

