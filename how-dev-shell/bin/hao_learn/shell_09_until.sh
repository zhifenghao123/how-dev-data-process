#!/usr/bin/env sh
<<INFO
SCRIPT:shell_09_until.sh
AUTHOR:haozhifeng
DATE:2024-01-09
DESCRIBE:shell 流程控制-until语句
INFO

#一、循环语句-until
#  系统中还有一个类似while的循环语句，大家可以看看until语句，不同于while的是，当条件为假时开始until循环。
#
#  1.1、until介绍
#  特点：条件为假就进入循环；条件为真就退出循环
#
#  1.2、until语法
#  until expression   [ 1 -eq 1 ]  (( 1 >= 1 ))
#    do
#      command
#      command
#      ...
#    done

# 使用while循环和until循环打印数字接龙，要求while循环输出1-5，until循环输出6-9.
i=1
while [ $i -le 5 ]
do
	echo $i
	let i++
	until [ $i -le 5 ]
		do
		    echo $i
	            let i++
	            [ $i -eq 10 ]&&break
	done
done