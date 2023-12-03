在Scala中，下划线的作用：
（1）import
    import xxx.YYY._

（2）可以声明变量
    val _ = "zhangsan"

（3）可以将函数当做对象使用
    val obj = func _

（4）如果匿名函数的参数按照顺序只使用一次，那么采用下划线代替
    _ + _

（5）import类时，使用下划线代替java中的星号
    java语法：import java.util.*;
    Scala语法： import java.util._

（6）import类时，用于屏蔽类
     import java.sql.{Date=>_, _}