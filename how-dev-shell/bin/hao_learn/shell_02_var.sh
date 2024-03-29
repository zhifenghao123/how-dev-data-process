#!/usr/bin/env sh
<<INFO
SCRIPT:shell_02_var.sh
AUTHOR:haozhifeng
DATE:2024-01-05
DESCRIBE:shell中变量的定义与使用
INFO

printenv

<<COMMENT
1、变量定义
  1.1、什么时候需要定义变量？
    如果某个内容需要多次使用，并且在代码中重复出现，那么可以用变量代表该内容。这样在修改内容的时候，仅仅需要修改变量的值。
    在代码运作的过程中，可能会把某些命令的执行结果保存起来，后续代码需要使用这些结果，就可以直接使用这个变量。

  1.2、定义一个变量
    变量格式： 变量名=值

    在shell编程中的变量名和等号之间不能有空格。

    变量名命名规则：
        命名只能使用英文字母，数字和下划线，首个字符不能以数字开头。
        中间不能有空格，可以使用下划线（_）。
        不能使用标点符号。
        不能使用bash里的关键字（可用help命令查看保留关键字）。
    定义变量举例：
    VAR1=1
    age=18 整形
    name=‘haozf’ 字符串
    score=88.8 浮点

    注意：字符串要用单引号或双引号引起来
    建议变量名为大写，和命令区分
          _name
    定义变量演示：

    变量赋值，此种方法设置为本地变量
    [root@haozhifeng ~]# name="haozf"
    [root@haozhifeng ~]# school='ayitula'
    [root@haozhifeng ~]# age=30
    [root@haozhifeng ~]# score=88.8

  2.3、取消变量 unset
    取消当前环境中的变量，如果是变量设置是保存在文件中，下次重启又会恢复
    [root@haozhifeng ~]# unset name
    [root@haozhifeng ~]# echo $name

  2.4、 有类型变量 declare
    -i 将变量看成整数
    -r 使变量只读 readonly,该变量的值无法改变，并且不能为unset
    -x 标记变量通过环境导出 export
    -a 指定为索引数组（普通数组）；查看普通数组
    -A 指定为关联数组；查看关联数组

    [root@haozhifeng ~]# declare -i num='asa'
    [root@haozhifeng ~]# echo $num
    0
    [root@haozhifeng ~]# num=100
    [root@haozhifeng ~]# echo $num
    100
    [root@haozhifeng ~]# declare -r num
    [root@haozhifeng ~]# echo $num
    100
    [root@haozhifeng~]# num=200
    -bash: num: 只读变量

    [root@haozhifeng ~]# declare -x
    declare -x HISTCONTROL="ignoredups"
    declare -x HISTSIZE="1000"
    declare -x HOME="/root"
    declare -x HOSTNAME="Bai_Shuming"
    declare -x LANG="zh_CN.UTF-8"
    declare -x LESSOPEN="||/usr/bin/lesspipe.sh %s"

  三、变量分类
      系统中的变量根据作用域及生命周期可以分为四类：本地变量、环境变量、全局变量、内置变量

    3.1、本地变量
      用户自定义的变量，定义在脚本或者当前终端中，脚本执行完毕或终端结束变量失效。

    3.2、环境变量
      定义在用户家目录下的.bashrc或.bash_profile文件中，用户私有变量，只能本用户使用。

      查看当前用户的环境变量 env

      查询当前用户的所有变量(临时变量与环境变量) set

    3.3、将当前变量变成环境变量 export
      定义一个临时变量
    1、
      [root@haozhifeng tmp]# export A=hello //临时将一个本地变量（临时变量）变成环境变量
      [root@haozhifeng tmp]# env|grep ^A
    A=hello
    2、
      [root@haozhifeng tmp]# A=HELLO
      [root@haozhifeng tmp]# export A

    3、定义一个永久生效变量：
      vim .bash_profile 或者 ~/.bashrc
      A=hello


    关于export说明
    用户登录时:
    1) 用户登录到Linux系统后，系统将启动一个用户shell。在这个shell中，可以使用shell命令或声明变量，也可以
    创建并运行 shell脚本程序。

    运行脚本时:
    2) 运行shell脚本程序时，系统将创建一个子shell。此时，系统中将有两个shell，一个是登录时系统启动的shell，
    另一个是系统为运行脚本程序创建的shell。当一个脚本程序运行完毕，它的脚本shell将终止，可以返回到执行该脚本
    之前的shell。

    从这种意义上来说，用户可以有许多 shell，每个shell都是由某个shell（称为父shell）派生的。
    在子shell中定义的变量只在该子shell内有效。如果在一个shell脚本程序中定义了一个变量，当该脚本程序运行时，
    这个定义的变量只是该脚本程序内的一个局部变量，其他的shell不能引用它，要使某个变量的值可以在其他shell中
    被改变，可以使用export命令对已定义的变量进行输出。

    export命令将使系统在创建每一个新的shell时定义这个变量的一个拷贝。这个过程称之为变量输出。

    3.4、全局变量
      使用export命令将本地变量输出为当前shell中的环境变量
      所有用户及shell都可以使用，可以在/etc/profile /etc/bashrc下永久定义

      打印全局变量 printenv

      定义格式
      export SCHOOL='haozhifeng'


      测试方法：
      通过不同用户登录测试是否能读取变量
    3.5、内置变量
      系统变量(内置bash中变量) ： shell本身已经固定好了它的名字和作用.

      $?：上一条命令执行后返回的状态，当返回状态值为0时表示执行正常，非0值表示执行异常或出错
       若退出状态值为0，表示命令运行成功
       若退出状态值为127,表示command not found
       若退出状态值为126,表示找到了该命令但无法执行（权限不够）
       若退出状态值为1&2,表示没有那个文件或目录

      $$：当前所在进程的进程号     echo $$   eg：kill -9 `echo $$`  = exit   退出当前会话

      $!：后台运行的最后一个进程号  （当前终端）  # gedit &
      !$ 调用最后一条命令历史中的参数
      !! 调用最后一条命令历史


      $#：脚本后面接的参数的个数
      $*：脚本后面所有参数，参数当成一个整体输出，每一个变量参数之间以空格隔开
      $@: 脚本后面所有参数，参数是独立的，也是全部输出

      $0：当前执行的进程/程序名  echo $0
      $1~$9 位置参数变量
      ${10}~${n} 扩展位置参数变量  第10个位置变量必须用{}大括号括起来
      ./1.sh a b c

      [root@haozhifeng shell01]# cat 2.sh
      #!/bin/bash
      #xxxx
      echo "\$0 = $0"
      echo "\$# = $#"
      echo "\$* = $*"
      echo "\$@ = $@"
      echo "\$1 = $1"
      echo "\$2 = $2"
      echo "\$3 = $3"
      echo "\$11 = ${11}"
      echo "\$12 = ${12}"

      了解$*和$@的区别：
      $* :表示将变量看成一个整体
      $@ :表示变量是独立的

      #!/bin/bash
      for i in "$@"
      do
      echo $i
      done

      echo "======我是分割线======="

      for i in "$*"
      do
      echo $i
      done

      [root@haozhifeng shell01]# bash 3.sh a b c
      a
      b
      c
      ======我是分割线=======
      a b c
      变量总结说明：

      本地变量：当前用户自定义的变量。当前进程中有效，其他进程及当前进程的子进程无效。

      环境变量：当前进程有效，并且能够被子进程调用。

      全局变量：全局所有的用户和程序都能调用，且继承，新建的用户也默认能调用.

      内置变量：shell本身已经固定好了它的名字和作用.

      变量类型	作用域	生命周期
      本地变量	当前shell环境(子shell不能用)	脚本结束或终端结束
      环境变量	当前shell或者子shell	当前进程结束
      全局变量	所有用户及shell环境	关机
      内置变量	所有用户及shell环境	关机


  四、变量取值
    读取变量内容符: 读取方法：变量名

    变量内容读出
    [root@haozhifeng ~]# echo $name
    haozf
    [root@haozhifeng ~]# echo $school
    ayitula
    [root@haozhifeng ~]# echo $age
    30
    [root@haozhifeng ~]# echo $score
    88.8
    注意

    变量读取过程中，默认单引号是不解释变量的.比如
    [root@haozhifeng ~]# echo '$name'
    $name

    如果必须使用单引号还要读取变量的值可以使用eval命令[重新运算求出参数的内容]
    [root@haozhifeng ~]# eval  echo '$name'
    haozf

  五、其他变量（扩展）
    1）取出一个目录下的目录和文件：dirname和 basename
    2）变量"内容"的删除和替换
    一个“%”代表从右往左去掉一个/key/
    两个“%%”代表从右往左最大去掉/key/
    一个“#”代表从左往右去掉一个/key/
    两个“##”代表从左往右最大去掉/key/

    # A=/root/Desktop/shell/mem.txt
    # echo $A
    /root/Desktop/shell/mem.txt
    # dirname $A   取出目录
    /root/Desktop/shell
    # basename $A  取出文件
    mem.txt

    # url=www.taobao.com
    # echo ${#url}      获取变量的长度
    # echo ${url#*.}       以分隔符.界限  *匹配所有
    # echo ${url##*.}
    # echo ${url%.*}
    # echo ${url%%.*}
COMMENT

