package com.howdev.flinklearn.tablesql;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.annotation.InputGroup;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;

import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.call;

public class FlinkUdfTableFuncLearn {
    public static void main(String[] args) throws Exception {

        // 创建流执行环境
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 创建带有WebUI的本地流执行环境
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        DataStreamSource<String> orderRecordDataStreamSource = env.fromElements(
               "hello java",
                "hello scala",
                "hello hadoop",
                "hello spark",
                "hello flink",
                "spark flink"
        );


        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        Table programWordsTable = tableEnv.fromDataStream(orderRecordDataStreamSource, $("words"));
        tableEnv.createTemporaryView("program_words", programWordsTable);

        // 2.注册 自定义函数
        tableEnv.createFunction("TableSplitFunction", TableSplitFunction.class);

        // 3.调用自定义函数
        // 3.1 sql用法

        tableEnv
                // (1)交叉连接
                //.sqlQuery("SELECT words, word, length from program_words, lateral table(TableSplitFunction(words))")
                // (2) 带 on true条件的左连接
                //.sqlQuery("SELECT words, word, length from program_words left join lateral table(TableSplitFunction(words)) on true")
                // (3) 重命名字段名
                .sqlQuery("SELECT words, newword, newlength from program_words left join lateral table(TableSplitFunction(words)) as T(newword, newlength) on true")
                .execute() // 调用了sql的execute，就不需要调用env的execute了
                .print();

        // 3.1 table api用法
//        orderRecordTable.select(call("HashFunction", $("userId")))
//                .execute()
//                .print();


    }

    // 1.定义 自定义函数的实现类
    // 类型标注，Row包含两个字段word和length
    @FunctionHint(output = @DataTypeHint("Row<word STRING, length INT>"))
    public static class TableSplitFunction extends TableFunction<Row> {
        // 返回时void，用collect输出
        public void eval(String str) {
            for (String word : str.split(" ")) {
                collect(Row.of(word, word.length()));
            }
        }
    }
}
