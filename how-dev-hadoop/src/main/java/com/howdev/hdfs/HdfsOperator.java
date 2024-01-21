package com.howdev.hdfs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * HdfsOperator class
 *
 * @author haozhifeng
 * @date 2024/01/21
 */
public class HdfsOperator {
    public static void main(String[] args) throws IOException {
        // 我们对HDFS的操作，使用的用户默认是当前系统登录的用户（比如当前在mac上操作，就是用mac上当前的用户名）
        // 因此，使用不正确的用户进行HDFS写操作的时候，会出现权限不足的问题
        // 解决的方案：（1）修改需要操作的路径权限为777；（2）修改需要操作的路径所属用户；（3）手动设置操作集群的用户名
        FileSystem hadoopFileSystem = getHadoopFileSystem();
        //uploadFileToHdfs(hadoopFileSystem);
        downloadFileFromHdfs(hadoopFileSystem);
    }

    public static FileSystem getHadoopFileSystem() throws IOException {
        // 创建配置文件对象，加载配置文件中的配置信息
        // 默认读取 core-default.xml hdfs-default.xml mapred-default.xml yarn-default.xml
        // 如果项目中有配置文件，则继续读取项目中的配置文件 core-site.xml hdfs-site.xml mapred-site.xml yarn-site.xml
        // 读取完成之后，也可以自己去配置信息
        // 属性优先级：代码中的设置＞*-site.xml > *-default.xml

        // 设置操作Hadoop的用户是谁
        System.setProperty("HADOOP_USER_NAME", "hadoop_user");

        Configuration conf = new Configuration();

        conf.set("fs.defaultFS", "hdfs://127.0.0.1:9820");

        FileSystem fileSystem = FileSystem.get(conf);
        System.out.println(fileSystem.getClass().getName());
        return fileSystem;
    }

    public static void uploadFileToHdfs(FileSystem fileSystem) throws IOException {
        Path srcPath = new Path("how-dev-hadoop/test-data/word.txt");
        Path destPath = new Path("/");
        fileSystem.copyFromLocalFile(srcPath, destPath);
    }

    public static void downloadFileFromHdfs(FileSystem fileSystem) throws IOException {
        Path srcPath = new Path("/word.txt");
        Path destPath = new Path("how-dev-hadoop/test-data/download-temp/word.txt");
        fileSystem.copyToLocalFile(srcPath, destPath);
    }

}
