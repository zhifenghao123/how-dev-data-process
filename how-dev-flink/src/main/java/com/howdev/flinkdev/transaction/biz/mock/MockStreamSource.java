package com.howdev.flinkdev.transaction.biz.mock;

import com.howdev.flinkdev.transaction.biz.domain.Transaction;
import com.howdev.flinkdev.transaction.biz.bo.GenerateTransactionUtil;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.flink.types.Row;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class MockStreamSource {

    public static class MyParallelRowStreamSource implements ParallelSourceFunction<Row> {

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
                Transaction transaction = GenerateTransactionUtil.generateTransaction();
                String currentTimeStr = sdf.format(new Date());
                //String currentTimeStr = sdf.format(transaction.getOccurredTime());

                Row row = Row.of(id, transaction.getUserId(), transaction.getAmount(), transaction.getType(), transaction.getOccurredLocation(), currentTimeStr, transaction.getBalance());
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

    public static class MyParallelTransactionStreamSource implements ParallelSourceFunction<Transaction> {

        /**
         * A flag that indicates the running state.
         */
        private boolean running = false;
        //long id = 0L;
        private static AtomicLong id = new AtomicLong(0L);


        public void run(SourceContext<Transaction> sourceContext) throws Exception {
            running = true;
            //long id = 0L;


            while (running) {
                Transaction transaction = GenerateTransactionUtil.generateTransaction();

                long nextId = id.getAndIncrement();
                if (nextId == Long.MAX_VALUE) {
                    id.set(0);
                }
                transaction.setId(nextId);

//                transaction.setId(id);
//                id += 1;
//                if (id == Long.MAX_VALUE) {
//                    id = 0;
//                }


                Date currentTime = new Date();
                transaction.setOccurredTime(currentTime);
                transaction.setOccurredTimeStamp(currentTime.getTime());

                sourceContext.collect(transaction);


                //System.out.println(transaction);

                // 每秒发送两4数据
                Thread.sleep(250);
            }
        }

        public void cancel() {
            running = false;
        }
    }

    public static class MyParallelTransactionStrStreamSource implements ParallelSourceFunction<String> {

        /**
         * A flag that indicates the running state.
         */
        private boolean running = false;
        //long id = 0L;
        private static AtomicLong id = new AtomicLong(0L);


        public void run(SourceContext<String> sourceContext) throws Exception {
            running = true;
            //long id = 0L;


            while (running) {
                Transaction transaction = GenerateTransactionUtil.generateTransaction();

                long nextId = id.getAndIncrement();
                if (nextId == Long.MAX_VALUE) {
                    id.set(0);
                }
                transaction.setId(nextId);

//                transaction.setId(id);
//                id += 1;
//                if (id == Long.MAX_VALUE) {
//                    id = 0;
//                }


                Date currentTime = new Date();
                transaction.setOccurredTime(currentTime);
                transaction.setOccurredTimeStamp(currentTime.getTime());

                sourceContext.collect(transaction.toJsonString());


                //System.out.println(transaction);

                // 每秒发送两4数据
                Thread.sleep(250);
            }
        }

        public void cancel() {
            running = false;
        }
    }


    public static void main(String[] args) throws Exception {
//        MyParallelRowStreamSource myParallelRowStreamSource = new MyParallelRowStreamSource();
//        myParallelRowStreamSource.run(null);
        MyParallelTransactionStreamSource myParallelTransactionStreamSource = new MyParallelTransactionStreamSource();
        myParallelTransactionStreamSource.run(null);
    }

}
