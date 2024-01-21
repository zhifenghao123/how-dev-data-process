package com.howdev.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * WordCountDriver class
 * 驱动类，需要完成：
 * 1. 创建Job对象
 * 2. 设置Job的属性
 * 3. Job的提交
 *
 * 程序最终打包后，Job提交命令：
 * hadoop jar how-dev-hadoop-1.0-SNAPSHOT.jar com.howdev.mapreduce.WordCountDriver /word.txt /output
 *
 *
 * @author haozhifeng
 * @date 2024/01/21
 */
public class WordCountDriver {
    public static void main(String[] args) throws Exception {
        // main1(args);
        // main2(args)
        main3(args);

    }

    public static void main1(String[] args) throws Exception {
        // 1. 创建Job对象
        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "hdfs://localhost:9820");
        Job job = Job.getInstance();

        // 2. 设置Job的属性
        // 在MapReduce中，用于处理MapTask、ReduceTask、驱动的类
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);
        job.setJarByClass(WordCountDriver.class);

        // 在Map阶段，输出的key、value的类型
        // 如果输出的<K2,V2>类型与<K3,V3>的类型相同，则可以省略
        //        job.setMapOutputKeyClass(Text.class);
        //        job.setMapOutputValueClass(IntWritable.class);

        // 在Reduce阶段，输出的key、value的类型(其实也是整个程序最终输出的key、value类型)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 设置Job的输入和输出
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        // 3. 提交Job
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static void main2(String[] args) throws Exception {
        // 1. 创建Job对象
        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "hdfs://localhost:9820");
        Job job = Job.getInstance();

        // 2. 设置Job的属性
        // 在MapReduce中，用于处理MapTask、ReduceTask、驱动的类
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);
        job.setJarByClass(WordCountDriver.class);

        // 在Map阶段，输出的key、value的类型
        // 如果输出的<K2,V2>类型与<K3,V3>的类型相同，则可以省略
        //        job.setMapOutputKeyClass(Text.class);
        //        job.setMapOutputValueClass(IntWritable.class);

        // 在Reduce阶段，输出的key、value的类型(其实也是整个程序最终输出的key、value类型)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 设置Job的输入和输出
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // 设置Job的分区器，用于决定ReduceTask的输出文件； 默认是HashPartitioner
        job.setPartitionerClass(WordCountPartitioner.class);

        // 设置ReuduceTask的数量，默认是1，决定了最终生成的文件的数量
        // 这个数量的设置，最好结合着Partitioner来设置，和分区器数量设置一直
        job.setNumReduceTasks(3);

        // 3. 提交Job
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static void main3(String[] args) throws Exception {
        // 1. 创建Job对象
        Configuration configuration = new Configuration();
        // configuration.set("fs.defaultFS", "hdfs://localhost:9820");

        /**
         * 在IDEA中运行MapReduce程序，可以选择计算资源、选择文件系统
         * 1. 选择计算资源
         *     mapreduce.framework.name，默认是local
         *     （1）local，使用的本机计算资源（CPU、内存），不会提交到集群中运行
         *     （2）yarn，使用集群的资源，提交到集群中运行
         * 2. 选择文件系统
         *     fs.defaultFS
         *     （1）hdfs://localhost:9820，使用HDFS文件系统
         *     （2）file:///，使用本地文件系统
         */
        // 如下两个属性可以两两组合，选择计算资源、选择文件系统
        configuration.set("mapreduce.framework.name", "local");
        configuration.set("fs.defaultFS", "file:///");

        Job job = Job.getInstance();
        // 2. 设置Job的属性
        // 在MapReduce中，用于处理MapTask、ReduceTask、驱动的类
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);
        job.setJarByClass(WordCountDriver.class);

        // 在Map阶段，输出的key、value的类型
        // 如果输出的<K2,V2>类型与<K3,V3>的类型相同，则可以省略
        //        job.setMapOutputKeyClass(Text.class);
        //        job.setMapOutputValueClass(IntWritable.class);

        // 在Reduce阶段，输出的key、value的类型(其实也是整个程序最终输出的key、value类型)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 设置Job的输入和输出
        FileInputFormat.setInputPaths(job,new Path("how-dev-hadoop/test-data/wordcount_input/word.txt"));
        FileOutputFormat.setOutputPath(job, new Path("how-dev-hadoop/test-data/wordcount_output"));

        // 设置Job的分区器，用于决定ReduceTask的输出文件； 默认是HashPartitioner
        job.setPartitionerClass(WordCountPartitioner.class);

        // 设置ReuduceTask的数量，默认是1，决定了最终生成的文件的数量
        // 这个数量的设置，最好结合着Partitioner来设置，和分区器数量设置一直
        job.setNumReduceTasks(3);

        // 3. 提交Job
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
