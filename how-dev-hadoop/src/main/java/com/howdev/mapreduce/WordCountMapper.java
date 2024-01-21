package com.howdev.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * WordCountMapper class
 *
 * Map阶段开始的时候，每一个MapTask都会逐行读取分片中的数据，并将读取到的数据转换行程<K1,V1>
 *
 * 由于我们的计算程序，需要在不同节点之间进行移动，因此涉及到的<K1,V1>、<K2,V2>、<K3,V3>都必须是实现序列化的类型，
 * Hadoop提供了自己的一套序列化的机制Writable
 * byte => ByteWritable
 * short => ShortWritable
 * int => IntWritable
 * long => LongWritable
 * float => FloatWritable
 * double => DoubleWritable
 * boolean => BooleanWritable
 * String => Text
 *
 * K1:读取到的数据的行首字符的偏移量，需要设计为LongWritable
 * V1:读取到的行数据，需要设计为Text
 *
 *
 * K2:经过逻辑处理之后，需要写出的键值对中，键的类型
 * V3:经过逻辑处理之后，需要写出的键值对中，值的类型
 *
 *
 * @author haozhifeng
 * @date 2024/01/21
 */
public class WordCountMapper  extends Mapper<LongWritable, Text, Text, IntWritable> {
    /**
     * 每当读取到一行数据，将其转换为<K1,V1>数据，调用这个方法
     *
     * @param key 行偏移量
     * @param value 行记录
     * @param context 上下文
     * @return:
     * @author: haozhifeng
     */
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        // 1.将读取到的一行数据，按照空格进行分割，切割出一个个单词
        String[] words = value.toString().split("\\s+");
        // 2.将切割出来的单词，按照<单词,1>的形式，写出
        for (String word : words) {
            context.write(new Text(word), new IntWritable(1));
        }

    }
}
