package com.howdev.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * WordCountReducer class
 * KEYIN: 其实就是map阶段输出的键的类型
 * VALUEIN: 其实就是map阶段输出的值的类型
 *
 * KEYOUT: 最终输出的键值对中键的类型
 * VALUEOUT: 最终输出的键值对中值的类型
 *
 * @author haozhifeng
 * @date 2024/01/21
 */
public class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    /**
     * Reduce阶段的处理逻辑
     *
     * @param key map阶段输出的键值对中的键
     * @param values 输入进这个方法之前，MapReduce会根据key进行分组，将相同的key对应的所有value都聚合在一起
     * @param context 操作上下文对象
     * @return:
     * @author: haozhifeng
     */
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        // 1、定义一个变量，统计单词出现的次数
        int count = 0;
        // 2、遍历values，累加单词出现的次数
        for (IntWritable value : values) {
            count += value.get();
        }
        // 3、将单词和次数结果输出
        context.write(key, new IntWritable(count));
    }
}