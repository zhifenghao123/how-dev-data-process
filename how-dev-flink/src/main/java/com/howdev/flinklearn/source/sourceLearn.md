在 Flink1.12以前，旧的添加Source的方式，是调用执行环境的 addSource()方法：
DataStream<String> stream = env.addSource (...) ;

方法传入的参数是一个“源函数”（source function），需要实现 SourceFunction 接口。

从Flink1.13开始，主要使用流批统一的新Source架构：

DataStreamSource<String> stream = env.fromSource (...) 
Flink 直接提供了很多预实现的接口，此外还有很多外部连接工具也帮我们实现了对应的Source，通常情况下足以应对我们的实际需求