package com.howdev.flinklearn.datastream.partition;

import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class PartitionLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(2);
        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据
        DataStreamSource<String> lineDataStreamSource = env.socketTextStream("127.0.0.1", 9999);

        // shuffle随机分区：random.nextInt(下游算子并行度)
        //lineDataStreamSource.shuffle().print();

        // rebalance 轮询分区：nextChannelToSendTo = (nextChannelToSendTo +1) % 下游算子并行度
        // 如果是 数据源倾斜的场景，source读进来之后，可以调用rebalance()方法，让数据源重新分配数据，解决数据源的数据倾斜问题
        //lineDataStreamSource.rebalance().print();

        // rescale 缩放：实现轮训，局部组队，比rebalance 更高效
        //lineDataStreamSource.rescale().print();

        // broadcast 广播：发送给下游的所有子任务
        // lineDataStreamSource.broadcast().print();

        // global 全局：全部发往下游第一个子任务
        //lineDataStreamSource.global().print();

        lineDataStreamSource.partitionCustom(new Partitioner<String>() {
            @Override
            public int partition(String key, int numPartitions) {
                return Integer.parseInt(key) % numPartitions;
            }
        }, data -> data).print();

        env.execute();
    }
}
