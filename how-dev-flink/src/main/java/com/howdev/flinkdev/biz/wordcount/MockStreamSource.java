package com.howdev.flinkdev.biz.wordcount;

import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.flink.types.Row;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MockStreamSource {

    public static class MyParallelRowStreamSource implements ParallelSourceFunction<Row> {

        public static final List<String> wordLexicon = Arrays.asList("Hello", "World", "Java", "Scala", "Python",
                "Hadoop", "MapReduce", "Flink", "Spark", "Storm", "Kafka", "HBase", "Hive", "HDFS", "Zookeeper");

        /**
         * A flag that indicates the running state.
         */
        private boolean running = false;

        public void run(SourceContext<Row> sourceContext) throws Exception {
            running = true;
            long id = 0L;

            // DateFormat df = DateFormat.getTimeInstance(DateFormat.LONG, Locale.CHINA);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


            while (running) {
                String currentTimeStr = sdf.format(new Date());
                Random random = new Random();
                int wordIndex = random.nextInt(wordLexicon.size() - 1);
                String word = wordLexicon.get(wordIndex);

                Row row = Row.of(id, word, currentTimeStr);
                sourceContext.collect(row);
                id += 1;
                // 只产生0-9的id
                if (id == Integer.MAX_VALUE) {
                    id = 0;
                }

                System.out.println(row);

                // 每秒发送两4数据
                Thread.sleep(250);
            }
        }

        public void cancel() {
            running = false;
        }
    }

    public static void main(String[] args) throws Exception {
        MyParallelRowStreamSource myParallelRowStreamSource = new MyParallelRowStreamSource();
        myParallelRowStreamSource.run(null);
    }

}
