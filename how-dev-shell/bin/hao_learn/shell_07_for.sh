#!/usr/bin/env sh
<<INFO
SCRIPT:shell_07_for.sh
AUTHOR:haozhifeng
DATE:2024-01-09
DESCRIBE:shell 流程控制-for循环语句
INFO

#一、循环语句-for
#  1.1 for基本语法 for条件循环
#  列表for循环：用于将一组命令执行已知的次数，下面给出了for循环语句的基本格式：
#  for variable_name in {list}
#     do
#          command
#          command
#          …
#     done
#  或者
#  for variable in a b c
#       do
#           command
#           command
#       done

#  1.2 for条件应用
#  for条件不同的赋值方式
#  a、赋值来自一个范围
#
#  for var in {1..10}
#    do
#        echo $var
#  done
#  b、直接赋值
#
#  for var in 1 2 3 4 5
#    do
#        echo $var
#  done
#  c、赋值来自命令
#
#  for var in `seq  10`
#    do
#        echo $var
#  done

for var in $(seq 10)
	do
			echo $var
done

for var in {0..10..2}
	do
			echo $var
done


for var in {10..1}
	do
			echo $var
done


for var in {10..1..-2}
	do
			echo $var
done


for var in `seq 10 -2 1`
	do
			echo $var
done

#  1.3不带列表循环
#  不带列表的for循环执行时由用户指定参数和参数的个数，下面给出了不带列表的for循环的基本格式：
#  for variable
#      do
#          command
#          command
#          …
#     done

#  1.4、for C格式语法
#  类C风格的for循环
#  for(( expr1;expr2;expr3 ))
#    do
#      command
#      command
#      …
#    done
  for (( i=1;i<=5;i++))
    do
      echo $i
    done

#
#  expr1：定义变量并赋初值   变量初始值
#  expr2：决定是否进行循环（条件）  变量的条件
#  expr3：决定循环变量如何改变,决定循环什么时候退出 自增或自减运算
#
#
# 多变量用法 for ((A=1,B=10;A<10,B>1;A++,B--))
for ((i=1;i<=5;i++));do echo $i;done
for ((i=1;i<=10;i+=2));do echo $i;done
for ((i=2;i<=10;i+=2));do echo $i;done

#二、for嵌套
#  2.1、for嵌套if
for ((num=1;num<10;num++))
   do
     echo $num
     [ $num -eq 5 ]&& break
done

#  2.2、for嵌套for
#打印99乘法表，思考A*B的关系
for ((A=1;A<=9;A++))
  do
     for ((B=1;B<=$A;B++))
        do
           echo -n -e "$B*$A=$((A*B)) \t"
     done
     #换行
     echo
done


#三、for循环与数组
#  3.1、使用for循环遍历读出数组
name=('tom' 'jarry' 'harry' 'barry')
for i in 0 1 2 3
  do
      echo ${name[$i]}
 done

#  3.2、使用for循环进行数组存值
for i in `seq 0 9`
  do
     read -p "name: " name[$i]
 done

#四、循环控制-break语句
#作用: 终止循环，执行循环体后面的代码
for i in `seq 1 9`
  do
      echo $i
      if [ $i -eq 5 ]
 				then
	   			break
			fi
done

#五、循环控制-continue语句
#作用: 跳过某次循环，继续执行下一次循环；表示循环体内下面的代码不执行，重新开始下一次循环
for ((i=1;i<10;i++))
   do
       if [ $i -eq 5 ]
	  then
		continue
	else
		echo $i
	fi
done

#六、循环控制-sleep
#作用: 控制循环的节奏,控制循环频率
#当执行一个无限循环语句的时候，如果任意其循环那么该循环就会疯狂的消耗计算机的内存和CPU资源，消耗最大的就是CPU，所以一个循环不可能让其肆意循环，必须控制其循环的节奏，可以使用sleep语句来完成。
echo -n "倒计时: "
for i in `seq 9 -1 1`
   do
      echo -n -e "\b$i"
      sleep 1
done
echo
echo "执行完毕"

#七、参数控制命令-shift
#作用: 外部传参到循环时，参数管理命令
#使位置参数向左移动，默认移动1位，可以使用shift 2 传参要是N的整数倍

#八、脚本退出命令-exit
#作用: 退出程序并释放占用的系统资源




