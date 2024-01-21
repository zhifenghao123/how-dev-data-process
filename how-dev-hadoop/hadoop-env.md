为了方便开发测试，在本地快速搭建了伪分布式环境。
除了jdk、hadoop安装于系统环境变量配置、ssh配置等等。
需要修改一下如下几个配置：

（1）core-site.xml
```xml
<configuration>
    <property>
        <!-- 设置namenode节点 -->
        <!-- 注意：hadoop 1.x时代，默认端口是9000；hadoop 2.x时代，默认端口是8020；hadoop 3.x时代，默认端口是9820 -->
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9820</value>
    </property>
    <!-- hdfs的基础路基，被其他属性所依赖的一个基础路径-->
    <property>
        <name>hadoop.tmp.dir</name>
        <value>/Users/hzf/SoftwareDevelopment/DevelopmentTool/hadoop-3.2.3/temp_hao_hadoop</value>
    </property>
</configuration>
```

（2）hdfs-site.xml
```xml
<configuration>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
    <!-- secondarynamenode守护进程的http地址：主机名和端口号 -->
    <property>
        <name>dfs.namenode.secondary.http-address</name>
        <value>localhost:9868</value>
    </property>
    <!-- namenode守护进程的http地址：主机名和端口号 -->
    <property>
        <name>dfs.namenode.http-address</name>
        <value>localhost:9870</value>
    </property>
</configuration>
```

（3）hadoop-env.sh
```shell
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home

# Hadoop 3.x中，需要添加如下配置，设置启动集群角色的用户是谁
export HDFS_NAMENODE_USER=hadoop_user
export HDFS_DATANODE_USER=hadoop_user
export HDFS_SECONDARYNAMENODE_USER=hadoop_user
```

然后，格式化hdfs：
hdfs namenode -format

最后，启动集群：
start-dfs.sh

界面查看：
http://127.0.0.1:9870/



