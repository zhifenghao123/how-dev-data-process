package com.howdev.flinklearn.datastream.window;

import com.howdev.flinklearn.biz.bo.UserGenerator;
import com.howdev.flinklearn.biz.domain.User;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

public class CountWindowLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据:1,M,20,180,1
        SingleOutputStreamOperator<User> dataSource = env
                .socketTextStream("127.0.0.1", 9999)
                .map((MapFunction<String, User>) value -> {
                    String[] splits = value.split(",");
                    return UserGenerator.generate(splits[0],
                            Integer.valueOf(splits[1]),
                            Long.parseLong(splits[2]));
                });

        KeyedStream<User, String> keyedStream = dataSource.keyBy(User::getGender);

        // 基于计数的
        //滚动窗口，窗口长度为5个
        //WindowedStream<User, String, GlobalWindow> windowStream = keyedStream.countWindow(5);
        //滑动窗口，窗口长度为5个，滑动步长为2个。没经过一个步长，都有一个窗口触发输出，第一次输出在第二条数据来的时候
        WindowedStream<User, String, GlobalWindow> windowStream = keyedStream.countWindow(5, 2);
        //全局窗口，计数窗口的底层用的就是这个，需要自定义的时候才使用
        //WindowedStream<User, String, GlobalWindow> windowStream = keyedStream.window(GlobalWindows.create());



        SingleOutputStreamOperator<String> processedStream = windowStream.process(new ProcessWindowFunction<User, String, String, GlobalWindow>() {
            @Override
            public void process(String key, ProcessWindowFunction<User, String, String, GlobalWindow>.Context context, Iterable<User> elements, Collector<String> out) throws Exception {
                // 上下文中可以拿到很多信息
                long windowMaxTimestamp = context.window().maxTimestamp();
                String formattedMaxTimestamp = DateFormatUtils.format(windowMaxTimestamp, "yyyy-MM-dd HH:mm:ss");

                List<String> users = new ArrayList<>();
                elements.forEach(user -> {
                    users.add(user.toString());
                });
                long count = elements.spliterator().estimateSize();

                String formattedOutput =String.format("key=%s,windowMaxTimestamp=%s 的窗口包含%d个元素 ===> %s", key, formattedMaxTimestamp, count, users);

                out.collect(formattedOutput);

            }
        });

        processedStream.print();

        env.execute();
    }
}
