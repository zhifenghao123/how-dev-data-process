package com.howdev.mock.stream;

import com.howdev.mock.dto.Transaction;
import com.howdev.mock.util.GenerateTransactionUtil;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.flink.types.Row;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MockStreamSource {

    public static class MyParallelRowStreamSource implements ParallelSourceFunction<Row> {

        /**
         * A flag that indicates the running state.
         */
        private boolean running = false;

        public void run(SourceContext<Row> sourceContext) throws Exception {
            running = true;
            long id = 0L;

            DateFormat df = DateFormat.getTimeInstance(DateFormat.LONG, Locale.CHINA);

            while (running) {
                Transaction transaction = GenerateTransactionUtil.generateTransaction();
                // Date currentTime = new Date();
                //String currentTime = df.format(new Date());
                String currentTime = df.format(transaction.getOccurredTime());

                Row row = Row.of(id, transaction.getUserId(), transaction.getAmount(), transaction.getType(), transaction.getOccurredLocation(), currentTime);
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
