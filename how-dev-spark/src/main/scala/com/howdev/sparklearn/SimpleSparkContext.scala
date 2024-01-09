package com.howdev.sparklearn

import org.apache.spark.{SparkConf, SparkContext}

/**
 * SparkContext构造简化工具类
 * 继承SimpleSpark后
 * 1. 直接使用sparkContext
 * 2. 可以overwrite sparkConfOpts来自定义新的配置
 * 3. setAppName(name)来动态传appName, 默认为当前类名
 */
trait SimpleSparkContext extends BaseEnv {
  @transient
  protected implicit lazy final val sparkContext = new SparkContext(sparkConf)
}

trait BaseEnv extends Serializable {
  private var _appName = this.getClass.getSimpleName.filter(!_.equals('$'))

  protected final def appName: String = _appName
  protected def setAppName(name: String): Unit = _appName = name

  protected def sparkConfOpts: Map[String, String] = Map.empty

  protected def sparkConf = {
    // 程序中直接设置为本地模式了，最终提交作业时如果是制定了集群模式，还是会以集群模式运行
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(_appName)
      .setAll(sparkConfOpts)
    sparkConf
  }

}
