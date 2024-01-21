package com.howdev.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * WordCountPartitioner class
 * Partitioner: 分区器，可以将map的输出结果按照制定的规则进行分区
 * 默认是HashPartitioner，根据key的hashcode 对reduce task的数量取模 进行分区
 *
 * 泛型，对应的是map输出的key和value
 *
 * @author haozhifeng
 * @date 2024/01/21
 */
public class WordCountPartitioner extends Partitioner<Text, IntWritable> {
    /**
     * 每一个map方法输出的键值对，都会调用一次getPartition方法，来确定分区号
     * 
     * @param text text
     * @param intWritable intWritable
     * @param i ReduceTask数量
     * @return: 返回分区号, 需要从0开始，且是连续的
     * @author: haozhifeng     
     */
    @Override
    public int getPartition(Text text, IntWritable intWritable, int i) {
        char firstChar = text.toString().charAt(0);
        if (firstChar >= 'a' && firstChar <= 'i') {
            return 0;
        } else if (firstChar >= 'j' && firstChar <= 'q') {
            return 1;
        } else {
            return 2;
        }
    }
}
