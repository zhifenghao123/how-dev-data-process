package com.howdev.sparklearn_java.sparkcore.rdd.operator.transformation;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Spark_Rdd_Transformation2 {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf();
        conf.setMaster("local");
        conf.setAppName(Spark_Rdd_Transformation2.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);

        List<List<Integer>> memoryData = Arrays.asList(Arrays.asList(1, 3, 2), Arrays.asList(4, 7, 5, 6));
        jsc.parallelize(memoryData, 3)
                // flatMap起到flat（扁平化）和map（映射）的作用
                .flatMap(new FlatMapFunction<List<Integer>, Integer>() {
                    @Override
                    public Iterator<Integer> call(List<Integer> integers) throws Exception {
                        List<Integer> result = new ArrayList<>();
                        for (Integer integer : integers) {
                            result.add(integer * 3);
                        }
                        result.addAll(integers);
                        return result.iterator();
                    }
                })
                // filter 可能造成数据倾斜，使用时需要注意
                .filter(value -> value > 2)
                // distinct 分布式（多个计算节点）去重，底层采用了分组+shuffle的处理方式。distinct底层会打乱数据
                .distinct()
                // sortBy 按照给定的规则排序对数据进行排序
                //     第一个参数：排序规则。 Spark会为每个数据增加一个标记，然后按照标记对数据进行排序
                //     第二个参数：是否按照降序排列
                //     第三个参数：排序的分区数
                // 如果分区数设置为7，那么排
                .sortBy(v1 -> v1, false, 3)
                // groupBy按照给定的规则分组，给定一个数，按照特定的分组返回对应的组名
                // Spark在数据处理中，要求同一个分组的数据必须在同一个节点上，所以groupBy操作会将操作的分区数据打乱重新组合。 就是会Shuffle操作
                // Spark要求所有的数据必须分组后才能继续执行后续操作，RDD对象不能保存数据，当前groupBy操作会将数据保存到磁盘文件中，保证数据全部分组后执行后续操作
                //.groupBy(v1 -> v1 % 2 == 0 ? "even" : "odd", 2)
                .collect().forEach(System.out::println);

        // 使主程序sleep，这样SparkContext不会被关闭，可以看到Spark的运行监控情况(http://localhost:4040/)
        Thread.sleep(1000000);
        jsc.close();
    }
}

