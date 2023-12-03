package com.howdev.scala.oop;

/**
 * Oop2ImportJava class
 *
 * @author haozhifeng
 * @date 2023/12/03
 */
public class Oop2ImportJava {
    /**
     * Java
     * 导入：import
     * 用法：import java.util.ArrayList;
     *      import java.util.*;
     *      import static java.util.XXX.YYY;
     * 用途：
     *  （1）导类
     *  （2）静态导入
     *
     * 分析：
     *  （1）如果类名起得好，没有包的概念，导入就没有必要； 所以不是必须的
     *  （2）不方便，不好理解。
     *
     *  Scala的语法对java的进行了扩展，在Scala中
     *  （1）可以导入包
     *  （2）import关键字可以在任何地方导入。 但是import关键字在当前位置使用，那么其他位置就不起作用
     *  （3）Scala导入一个包中的所有类，采用下划线代替java中的星号
     *  （4）可以将一个包中的多个类在同一行导入
     *   (5）屏蔽类（如果不同的包中有相同的类名，可以通过屏蔽方式进行编译）
     *   (6）给类起别名
     *   (7）Scala中import的规则：以当前包为基准，导入指定子包中的类；如果找不到，再从顶级包中一次查找。
     *       如果就是要从定级包的子包中查找，需要特殊处理 print(new _root_.java.util.HashMap())
     *   (8)有些内容无需导入。
     *     Java中java.lang包中的类无需导入
     *     Scala中java.lang包中的类无需导入
     *     Scala中scala包中的类无需导入
     *     Scala中Predef对象的方法无需导入，类似于静态导入
     *
     */
}
