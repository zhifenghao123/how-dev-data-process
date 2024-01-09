#!/usr/bin/env sh
<<INFO
SCRIPT:shell_04_operation.sh
AUTHOR:haozhifeng
DATE:2024-01-05
DESCRIBE:shell中运算
INFO

#一、赋值运算
#  赋值运算符 =
a=10
#  重点：字符串必须用引号引起来
name='haozf'
echo $name


#二、算术运算[四则运算]
#  2.1 运算符与命令
#  （1）四则运算符： + - * \ 【加减乘除】
#  （2）扩展： % ** 【取余 开方】
#
#  （3）运算命令:
#    1）整形运算
#      expr
#      let
#      $(())
#      bc
#    2）浮点运算
#      bc
#  2.2 整形运算
#    （1）expr 命令：只能做整数运算，格式比较古板，注意空格
expr 1 + 1
expr 5 - 2
expr 5 \* 2  #注意*出现应该转义，否则认为是通配符
expr 5 / 2
expr 5 % 2
#（2）let命令:只能做整数运算，且运算元素必须是变量，无法直接对整数做运算
let a=100+3;echo $a
let a=100-3;echo $a
let a=100/3;echo $a
let a=100*3;echo $a
let a=100%3;echo $a
let a=100**3;echo $a
a=100
let a++;echo $a
let a--;echo $a
let a-=3;echo $a
let a+=5;echo $a
#（3）双小圆括号运算，在shell中(( ))也可以用来做数学运算
echo $(( 100+3))
echo $(( 100-3))
echo $(( 100%3))
echo $(( 100*3))
echo $(( 100/3))
echo $(( 100**3))     #开方运算

#  2.3 浮点运算
#  浮点运算是采用的命令组合的方式来实现的 echo “scale=N;表达式”|bc
echo "scale=2;3+100"|bc
echo "scale=2;100-3"|bc
echo "scale=2;100/3"|bc
echo "scale=2;100*3"|bc

#三、比较运算
# 计算机除了赋值和算数运算外，还有比较运算，比如说比较两个数的关系，比较两个字符串的关系【用户登录系统】等。接下来我们学习如何在shell中进行比较运算
#  3.1、整形比较运算
#  运算符解释：
#   精确比较
#          -eq         等于 equal
#          -gt         大于
#          -lt         小于
#   模糊比较
#          -ge         大于或等于
#          -le         小于或等于
#          -ne         不等于

# 备注：linux命令test只能比较两个整数的关系，不会返回结果，需要通过$?才能看到结果
test 100 -gt 300;echo $?
test 100 -ge 300;echo $?
test 100 -eq 300;echo $?
test 100 -le 300;echo $?
test 100 -lt 300;echo $?
test 100 -ne 300;echo $?

#3.2、字符串比较运算
#  3.2.1 字符串比较运算符
#  运算符解释，注意字符串一定别忘了使用引号引起来
#    ==          等于
#    !=          不等于
#    -n          检查字符串的长度是否大于0
#    -z          检查字符串的长度是否为0
#  3.2.2 比较两个字符串关系
test 'root' == 'root';echo $?
test 'root' != 'root1';echo $?
name=
test -n "$name";echo $?
test -z "$name";echo $?

#  四、逻辑运算
#    完成一个任务中需要多个条件都满足或者多个条件中只要满足一个即可，那么这就是我们的逻辑运算。
#    通过多个条件判断结果，才能得出结论
#
#  4.1、逻辑运算应用场景
#    多条件同时判断
#  4.2、逻辑运算符
#  1）逻辑与运算 &&
#  2）逻辑或运算 ||
#  3）逻辑非运算 ！
#2、交互输入
read -p "请输入你的存款: " money
read -p "请输入你的房子数量: " zhangse
read -p "请输入你车子的数量: " car

#3、判断应征条件，并给出结果
#姑娘20岁代码
if [ $1 -eq 20 ] && [ $money -gt 1000000 ] && [ $zhangse -ge 2 ] && [ $car -ge 1 ] && [ "$2" == "男" ];then
    echo "初始通过，等待姑娘复试吧"
#姑娘30岁代码
elif [ $1 -eq 30 ] && [ "$2" == "男" ] && ( [ $money -gt 1000000 ] || [ $zhangse -ge 2 ] || [ $car -ge 1 ] );then
    echo "初始通过，等待姑娘复试吧"
#姑娘40岁代码
elif [ $1 -eq 40 ] && [ ! $2 == "女" ];then
    echo "初始通过，等待姑娘复试吧"
else
   echo "你不满足条件,byebye"
fi


#五、文件判断[文件类型、权限、新旧判断]
#  linux的设计思路：一切皆文件，对文件系统的操作其实可以狭隘的理解为对文件的操作。如果希望对文件类型和权限或者两个文件做新旧或者是否同一个文件进行判断。
#
#  5.1、test判断命令
#  命令功能： 检测文件类型和比较运算
#  命令用法
#        test [命令选项] 表达式
#  命令选项
#    -d  检查文件是否存在且为目录
#    -e  检查文件是否存在
#    -f  检查文件是否存在且为文件
#    -r  检查文件是否存在且可读
#    -s  检查文件是否存在且不为空
#    -w  检查文件是否存在且可写
#    -x  检查文件是否存在且可执行
#    -O  检查文件是否存在并且被当前用户拥有
#    -G  检查文件是否存在并且默认组为当前用户组
#    -nt file1 -nt file2  检查file1是否比file2新
#    -ot file1 -ot file2  检查file1是否比file2旧
#    -ef file1 -ef file2  检查file1是否与file2是同一个文件，判定依据的是i节点
test -f /etc/passwd;echo $?
test -f /etc;echo $?
test -d /etc;echo $?
test -x /root/anaconda-ks.cfg ;echo $?
ll /root/anaconda-ks.cfg
test -r /root/anaconda-ks.cfg ;echo $?
test -w /root/anaconda-ks.cfg ;echo $?

#!/bin/bash
#
#Author: haozhifeng
#
#Release:
#Description:找到服务的PID号,如果服务开启则杀死，否则提示服务已经关闭或不存在

#1、判断PID
#注意PID的路径，如果服务的PID不在这里可以做个软连接
if [ -f /var/run/$1.pid ];then
   #2、如果存在
   PID=`cat /var/run/$1.pid`
   #3、统计进程数
   process_num=`ps aux|grep $PID|wc -l`
   #5、判断进程数大于2则杀死
   if [ $process_num -ge 2 ];then
       kill -s QUIT $PID
   else
   #5、判断小于2则提示进程不存在,同时删除服务PID文件
   	echo "service $1 is close"
        rm -f /var/run/$1.pid
   fi
else
   #2、不存在
   echo "service $1 is close"
fi