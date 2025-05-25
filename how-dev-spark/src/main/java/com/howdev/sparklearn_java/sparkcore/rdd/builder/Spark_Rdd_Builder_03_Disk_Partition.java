package com.howdev.sparklearn_java.sparkcore.rdd.builder;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.List;

public class Spark_Rdd_Builder_03_Disk_Partition {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf();
        conf.setMaster("local[4]");
        conf.setAppName(Spark_Rdd_Builder_03_Disk_Partition.class.getSimpleName());

        final JavaSparkContext jsc = new JavaSparkContext(conf);

        /**
         * 磁盘文件数据源
         * textFile 方法有两个参数，一个是数据源文件路径path，一个是【最小分区数】minPartitions。
         *
         * 1.分区数
         *
         * 如果不指定最小分区数minPartitions，Spark会根据默认值来决定minPartitions
         *      defaultMinPartitions: Int = math.min(defaultParallelism, 2)
         *      其中 defaultParallelism = scheduler.conf.getInt("spark.default.parallelism", totalCores)
         *
         * ！！！
         * Spark 框架是基于MapReduce开发的，Spark框架对文件的操作没有自己实现，而是采用MapReduce库（Hadoop）来实现的。
         * 因此，Spark读取文件时的切片数量，不是由Spark框架决定的，而是由Hadoop库决定的。
         *
         * Hadoop的切片规则
         * 以下面的例子，数据源为how-dev-spark/test-datasets/sparklearn_java/hello/in/number.txt
         * (文件内容只有1\n\r2\n\r3)
         * 指定minPartitions=3
         * totalSize: 文件总大小。 7 byte
         * goalSize: 每个切片要放的数据大小。以minPartitions=3为例，goalSize = totalSize/minPartitions = 7/3 = 2...1 byte= 2 byte
         * partitionNum: 切片数量。 partitionNum = totalSize/goalSize = 7 / 2 = 3...1  ，而1/3 = 33.33% > 10%，所以切片数量为3 + 1 = 4
         * 【如果剩余的数据占每个分片要放数据的百分比，小于等于10%，则会将剩余的数据放到最后一个分片中；大于10%，则会将剩余的数据放到新开的分区中】
         *
         * ------------------------
         * |  totalSize  |  calculate goalSize  |  goalSize  |         calculate partitionNum          | partitionNum  |
         * |   5 byte   |   5/3 = 1...2 byte    |   1 byte   |     5/2 = 2...1, 1 / 1 =10% <= 10%      |   2 + 1 = 3   |
         * |   7 byte   |   7/3 = 2...1 byte    |   2 byte   |     7/2 = 3...1, 1 / 2 =50% > 10%       |   3 + 1 = 4   |
         *
         *
         * 2.分区数据分配规则
         * Spark不支持文件操作，文件操作都是由Hadoop库来实现的。
         * Hadoop对文件切片数量的计算与对文件数据存储计算规则不一样：
         * 对分区数量计算的时候，考虑的是尽可能平均，计算是按字节数来计算的。
         * 对分区数据存储计算的时候，考虑的是业务数据的完整性，是按读行来读取。读取数据的时候，还要考虑数据偏移量，偏移量从0开始，且相同偏移量不能重复读取。
         *
         * 还是以下面的例子，数据源为how-dev-spark/test-datasets/sparklearn_java/hello/in/number.txt
         *  (文件内容只有1\n\r2\n\r3)
         *  但指定minPartitions=2
         *  则按照分区数的计算规则，partitionNum = 4
         *  partition0: 3 byte， [0, 3]  ，对应字节数据：1\n\r
         *  partition1: 3 byte， [3, 6]  ，对应字节数据：2\n\r
         *  partition2: 1 byte， [6, 7]  ，对应字节数据：3
         *  -------------------------
         *  行内容     偏移量
         *  1\n\r     012
         *  2\n\r     345
         *  3         6
         *  --------------------------
         *
         *  分区数据分配的规则：
         *  把分区数计算规则拿过来，
         *  partition0: 3 byte， [0, 3]，读取0到3偏移量的数据，且包含0和3偏移量，也就是应该读取到1\n\r2，但是偏移量3和偏移量4、5是同一行的，每次读取必须为一行，因此要读取上偏移量4、5，因此读取到1\n\r2\n\r3
         *  partition1: 3 byte， [3, 6]，因为偏移量3，4，5已经被读取过了，因此只能读取偏移量6，也就是3
         *  partition2: 1 byte， [6, 7]，偏移量只有7，只读取偏移量6，但是偏移量6已经被读取过了，因此读取不到数据
         *
         *  文件存储数据的时候，做到每一笔业务一行数据
         */
        JavaRDD<String> rdd = jsc.textFile("how-dev-spark/test-datasets/sparklearn_java/hello/in/number.txt", 2);

        final String outputPath = "how-dev-spark/test-datasets/sparklearn_java/hello/output"+"-"+System.currentTimeMillis();
        rdd.saveAsTextFile(outputPath);

        jsc.close();
    }
}
