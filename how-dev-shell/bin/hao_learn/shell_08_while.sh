#!/usr/bin/env sh
<<INFO
SCRIPT:shell_08_while.sh
AUTHOR:haozhifeng
DATE:2024-01-09
DESCRIBE:shell 流程控制-while循环
INFO

#一、while介绍
#特点：条件为真就进入循环；条件为假就退出循环，一般应用在未知循环次数的环境。
#  1.1、while语法
#  while [ 表达式 ]
#    do
#      command...
#    done
#
#  while  [ 1 -eq 1 ]    或者 (( 1 > 2 ))
#    do
#       command
#       command
#       ...
#   done

#备注： 知道循环次数就可以用for，比如说一天需要循环24次；如果不知道代码要循环多少次，那就用while，比如我们作业中要求写的猜数字，每个人猜对一个数字的次数都是不能固定的，也是未知的。
for ((i=1;i<=5;i++))
do
	echo $i
done

num=1
while [ $num -le 5 ]
  do
      echo $num
      let num++
done

#二、while与shell运算
#  2.1、比较运算
#  循环交互输入一个小写字母，按Q退出循环
read -p "请输入一个小写字母,按Q退出: " choose
while [ $choose != 'Q' ]
  do
     echo "你输入的是: $choose"
     read -p "请输入一个小写字母,按Q退出: " choose
done

#  2.2、逻辑运算
#丈母娘选女婿  分别按照姑娘20  30   40 进行与或非模拟

#1.第一个应征者回答
read -p "你有多少钱: " money
read -p "你有多少车: " car
read -p "你家房子有几套: " house


#while [ $money -lt 10000 ]&&[ $car -lt 1 ]&&[ $house -lt 2 ]
while [ $money -lt 10000 ]||[ $car -lt 1 ]||[ $house -lt 2 ]
  do
     #应征者不满住条件开始下一次循环
     echo "有请下一个"
     read -p "你有多少钱: " money
     read -p "你有多少车: " car
     read -p "你家房子有几套: " house
done

#应征者满足条件
echo  "乖女婿，你怎么才来啊！女儿给你了"


#  2.3、文件类型判断
#    使用循环判断/tmp/xxx目录下的文件，如果不是文件类型的打印字符串"目录"
while [ ! -f /tmp/xxx ]
 do
   echo “目录”
   sleep 1
done

#  2.4、特殊条件
#  while语句中可以使用特殊条件来进行循环：
#
#  符号":" 条件代表真，适用与无限循环
#  字符串 “true” 条件代表真，适用与无限循环
#  字符串 "false"条件代表假
while :
 do
   echo haha
   sleep 1
done


while true
 do
   echo haha
   sleep 1
done

#三、while与循环控制语句
#  3.1、sleep语句
#1.定义初始值
time=9

#2.循环输出，1秒一次
while [ $time -ge 0 ]
  do
     echo -n -e  "\b$time"
     let time--
     #控制循环 1秒一次
     sleep 1
done
#回车
echo

#  3.2、break
#1、定义初始值
num=1

while [ $num -lt 10 ]
  do
     echo $num
     #判断当前num的值，如果等于5就跳出循环
     if [ $num -eq 5 ]
        then
		break
     fi
     #自动累加
     let num++
done

# 3.3、continue
#1、定义初始值
num=0

while [ $num -lt 9 ]
  do
     #自动累加
     let num++
     #判断当前num的值，如果等于5就跳过本次循环
     if [ $num -eq 5 ]
        then
		continue
     fi
     #输出num的值
     echo $num
done

#四、while嵌套其他语句
#  4.1、while嵌套if
#1、定义初始值
num=1

while [ $num -lt 10 ]
  do
     echo $num

     #判断当前num的值，如果等于5就跳出循环
     if [ $num -eq 5 ]
        then
		break
     fi

     #自动累加
     let num++
done

# 4.2、while嵌套for
A=1
while [ $A -lt 10 ]
  do
    for ((B=1;B<=$A;B++))
       do
	  echo -n -e "$B*$A=$((A*B)) \t"
   done
   echo
   let A++
done

# 4.3、while嵌套while
#定义A
A=1
while [ $A -lt 10 ]
  do
      #定义B
      B=1
      while [ $B -le $A ]
        do
          echo -n -e "$B*$A=$((A*B)) \t"
          let B++
      done

   echo
   let A++
done






