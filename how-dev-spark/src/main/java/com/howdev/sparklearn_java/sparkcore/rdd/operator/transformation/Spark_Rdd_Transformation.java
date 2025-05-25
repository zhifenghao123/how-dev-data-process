package com.howdev.sparklearn_java.sparkcore.rdd.operator.transformation;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;
import java.util.List;

public class Spark_Rdd_Transformation {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf();
        conf.setMaster("local[2]");
        conf.setAppName(Spark_Rdd_Transformation.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);

        final List<Integer> numbers = Arrays.asList(1, 2, 3, 4);

        JavaRDD<Integer> rawRdd = jsc.parallelize(numbers, 2);

        JavaRDD<Integer> mappedRdd = rawRdd.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v) throws Exception {
                Thread.sleep(500);
                System.out.printf("map1(v -> 2 * v)#(%d * 2) = %d\n", v, v * 2);
                return v * 2;
            }
        });

        // 使用 lambda 表达式替代方法引用（Function 类型有@FunctionalInterface）
        //JavaRDD<Integer> mappedRdd = rawRdd.map(v1 -> v1 * 2);

        //JavaRDD<Integer> mappedRdd = rawRdd.map(NumberCalculator::multiTwo);

        JavaRDD<Integer> mappedRdd2 = mappedRdd.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v) throws Exception {
                Thread.sleep(500);
                System.out.printf("map2(v -> v * v)##(%d * %d ) = %d\n", v, v, v * v);
                return v * v;
            }
        });
//        mappedRdd.collect().forEach(System.out::print);
//        System.out.println("-----------------");
//        rawRdd.collect().forEach(System.out::print);
//        System.out.println("-----------------");
        // 对于同一个RDD，依次调用两次map算法，两次map算法的执行顺序是：逐个对元素执行完所有两个map算子，再对其他元素执行两个map算子。
        // 也就是同一个元素执行完两个map算子，再执行下一个元素。 并不是所有元素执行完第一个map算子，再对所有元素执行第二个map算子。
        mappedRdd2.collect().forEach(System.out::print);


        /**
         * Map算法
         * 默认情况下，新创建的RDD的分区数量和之前旧的RDD的数量保持一致
         * 默认情况下，数据流转所在的分区编号不变，数据流转的顺序不变
         *
         * Kafka存在多个分区，每个分区采用了类似于队列的处理方式，所以存在顺序（分区内有序，分区间无序）
         * Spark处理不同分区数据时：分区内有序，分区间无序
         */
//        final String outputPath = "how-dev-spark/test-datasets/sparklearn_java/hello/output"+"-"+System.currentTimeMillis();
//        rawRdd.saveAsTextFile(outputPath + "rawRdd");
//        mappedRdd.saveAsTextFile(outputPath + "mappedRdd");
//        mappedRdd2.saveAsTextFile(outputPath + "mappedRdd2");

        jsc.close();
    }
}

class NumberCalculator {
    public static Integer multiTwo(Integer v1) {
        return v1 * 2;
    }
}
