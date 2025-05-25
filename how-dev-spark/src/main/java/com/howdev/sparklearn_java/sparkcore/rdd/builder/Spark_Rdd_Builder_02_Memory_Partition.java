package com.howdev.sparklearn_java.sparkcore.rdd.builder;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

public class Spark_Rdd_Builder_02_Memory_Partition {
    public static void main(String[] args) {

        /**
         * local 模式下，分区数量和环境核数相关，默认是环境核数。但一般不推荐
         * 分区数量建议手动设定
         */
        SparkConf conf = new SparkConf();
        // conf.setMaster("local");
        //conf.setMaster("local[2]");
        conf.setMaster("local[*]");
        conf.set("spark.default.parallelism", "3");
        conf.setAppName(Spark_Rdd_Builder_02_Memory_Partition.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);

        final List<String> words = Arrays.asList("1", "2", "3", "4", "5", "6");
        /**
         * 内存数据源
         * parallelize 方法有两个参数，一个是数据源，一个是切片数量（Hadoop中的切片和Spark中的分区是一个概念）。
         * 1.分区数
         * 也就是可以通过第二个参数来控制分区数量。
         * 如果不指定切片数量，Spark会根据默认值来决定分区数量
         * 在local模式下，优先从配置对象中获取 spark.default.parallelism 的并行度值；如果没有配置，则使用当前环境总的虚拟核数。
         * LocalSchedulerBackend类中，scheduler.conf.getInt("spark.default.parallelism", totalCores)
         *
         * 也就是按照如下优先级顺序来决定分区数量：
         *  （1）优先使用parallelize方法的第二个参数
         *  （2）使用配置参数：spark.default.pardllelism
         *  （3）采用坏境默认总核数（虚拟核核数）
         *
         *  2.分区数据分配规则
         *  ParallelCollectionRDD # slice ##positions
         *  (0 until numSlices).iterator.map { i =>
         *         val start = ((i * length) / numSlices).toInt
         *         val end = (((i + 1) * length) / numSlices).toInt
         *         (start, end)
         *       }
         *   以 数据（"1", "2", "3", "4", "5", "6"）为例，numSlices = 4
         *   -------------
         *   |  sliceIndex  |  start = (i * length) / numSlices |  end=((i + 1) * length) / numSlices  |  [start, end) |
         *   |     0        |            0                      |           1                          |  [0, 1) |
         *   |     1        |            1                      |           3                          |  [1, 3) |
         *   |     2        |            3                      |           4                          |  [3, 4) |
         *   |     3        |            4                      |           6                          |  [4, 6) |
         */
        //JavaRDD<String> rdd = jsc.parallelize(words);
        JavaRDD<String> rdd = jsc.parallelize(words, 4);

        // 将RDD分区后的数据保存到磁盘文件中
        // saveAsTextFilek 方法的参数是文件的保存路径，可以是绝对路径，也可以是相对路径
        // Idea 中，默认的相对路径是项目的根目录
        final String outputPath = "how-dev-spark/test-datasets/sparklearn_java/hello/output"+"-"+System.currentTimeMillis();
        rdd.saveAsTextFile(outputPath);

        jsc.close();
    }
}
