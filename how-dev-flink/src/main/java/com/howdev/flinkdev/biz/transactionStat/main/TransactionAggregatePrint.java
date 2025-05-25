package com.howdev.flinkdev.biz.transactionStat.main;

import com.howdev.flinkdev.biz.transactionStat.domain.Transaction;
import com.howdev.flinkdev.biz.transactionStat.dto.TransactionAggregateResult;
import com.howdev.flinkdev.biz.transactionStat.mock.MockStreamSource;
import com.howdev.flinkdev.common.util.SerialUtil;
import com.howdev.flinkdev.biz.transactionStat.myoperator.MyAggregateFunction;
import com.howdev.flinkdev.biz.transactionStat.myoperator.MyProcessWindowFunction;
import com.howdev.flinkdev.flink.watermark.MyWatermarkStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.Map;

@Slf4j
public class TransactionAggregatePrint {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setString("rest.port","9001");

        // StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 创建带有WebUI的本地流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        DataStream<String> sourceDataStream = env.addSource(new MockStreamSource.MyParallelTransactionStrStreamSource());

        MyWatermarkStrategy<Transaction> watermarkStrategy = new MyWatermarkStrategy();

        // 过滤掉非request类型的数据
        FilterFunction<String> filterFunction = rawData -> {
            try {
                Map<String, String> map = SerialUtil.parseJson(rawData);
                return map.containsKey("userId")
                        && map.containsKey("amount");
            } catch (Exception e) {
                log.error("type error, raw data: " + rawData + ", error:" + e.getMessage());
                //e.printStackTrace();
            }
            return false;
        };

        // 原始数据映射为KafkaRawData对象
        DataStream<Transaction> transactionWithWatermarkDataStream = sourceDataStream
                .filter(filterFunction).rebalance()
                .map((MapFunction<String, Transaction>) Transaction::new).rebalance()
                .assignTimestampsAndWatermarks(watermarkStrategy.withIdleness(Duration.ofSeconds(5))).rebalance();

        // 创建键选择器，按照userId、occurredLocation 两个属性为聚合维度进行聚合选择
        KeySelector<Transaction, Tuple2<Long, String>> keySelector = new KeySelector<Transaction, Tuple2<Long, String>>() {
            @Override
            public Tuple2<Long, String> getKey(Transaction value) throws Exception {
                return new Tuple2<>(value.getUserId(), value.getOccurredLocation());
            }
        };

        // 使用keyBy进行分组
        KeyedStream<Transaction, Tuple2<Long, String>> keyedStream = transactionWithWatermarkDataStream.keyBy(keySelector);

        // 定义窗口并聚合计算
        SingleOutputStreamOperator<TransactionAggregateResult> aggregatedDataStream =
                keyedStream.window(TumblingEventTimeWindows.of(Time.minutes(1)))
                        .allowedLateness(Time.seconds(5))
                        .aggregate(new MyAggregateFunction(), new MyProcessWindowFunction());


        DataStream<TransactionAggregateResult> filteredDataStream = aggregatedDataStream.filter(
                new FilterFunction<TransactionAggregateResult>() {
                    @Override
                    public boolean filter(TransactionAggregateResult value) throws Exception {
                        //log.info("filter: " + value.toString());
                        return value.getTotalCount() >= 2;
                    }
                }
        );

        aggregatedDataStream.print();

        filteredDataStream.print();

        String jobName = TransactionAggregatePrint.class.getSimpleName();
        env.execute(jobName);

    }

}
