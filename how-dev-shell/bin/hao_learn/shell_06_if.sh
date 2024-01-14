#!/usr/bin/env sh
<<INFO
SCRIPT:shell_06_if.sh
AUTHOR:haozhifeng
DATE:2024-01-09
DESCRIBE:shell if条件判断
INFO

#一、单if语法
#if [ condition ]
#   then
#      commands
#fi
#
#二、if…else语句
#if [ condition ]
#     then
#          commands1
#else
#          commands2
#fi
#
#三、if…elif…else
#if [ condition 1]
#     then
#            command1
#elif [ condition 2]
#     then
#             commands2
#  .......
#else
#            commandsX
#fi
#
#四、if嵌套if
#当有多步判断的时候，可以使用if嵌套
#1）多步判断 类似于多条件if
#2）依赖执行的环境 configure->make->make install
#
#使用if嵌套if的方式判断两个整数的关系
if [ $1 -ne $2 ]
   then
       if [ $1 -gt $2 ]
  	  then
		echo " $1 > $2 "
       else
		echo " $1 < $2 "
       fi
else
       echo " $1 = $2 "
fi

#五、if高级用法
#  7.1、条件符号使用双圆括号，可以在条件中植入数学表达式 if (())
    if (( (5+5-5)*5/5 > 10 ))
        then
            echo "yes"
    else
            echo "no"
    fi
#  7.2、使用双方括号,可以在条件中使用通配符
#  通过代码看下 ，为字符串提供高级功能，模式匹配 r* 匹配r开头的字符串
  for var in  ab ac rx bx rvv vt
   do
       if [[ "$var" == r* ]]
	  then
		echo "$var"
       fi
  done

#六、简写if
#省去了关键字，条件为真采用&&符号链接命令块，条件为假采用||链接命令块
#简写if一般用在简单的判断中
if [ ! -d /tmp/baism ]
    then
        mkdir /tmp/baism
fi

#可以简写为
[ ！ -d /tmp/baism ] && mkdir /tmp/baism


if [ $USER == 'root' ]
	  then
	      echo "hello root"
else
			  echo "hello guest"
fi
#可以简写
[ $USER == 'root' ]&&echo "hello root" || echo "hello guest"



