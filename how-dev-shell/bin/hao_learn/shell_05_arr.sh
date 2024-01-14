#!/usr/bin/env sh
<<INFO
SCRIPT:shell_05_arr.sh
AUTHOR:haozhifeng
DATE:2024-01-05
DESCRIBE:shell数组
INFO

#一、数组介绍
#  数组可以让用户一次赋予多个值，需要读取数据时只需通过索引调用就可以方便读出了。
#  普通数组：只能使用整数作为数组索引(元素的索引)
#  关联数组：可以使用字符串作为数组索引(元素的索引)
#二、数组定义
#				数组名称=(元素1 元素2 元素3 ...)
#三、数组赋值方式
#  一次附一个值
#  变量名=变量值
  array[0]=v1
  array[1]=v2
  array[3]=v3
#  一次附多个值
  array=(var1 var2 var3 var4)
  array1=(`cat /etc/passwd`)			//将文件中每一行赋值给array1数组
  array2=(`ls /root`)
  array3=(harry amy jack "Miss zhang")
  array4=(1 2 3 4 "hello world" [10]=linux)
#四、数组取值
#  取值方式: ${数组名称[索引]}
#索引: 默认情况下索引是指数组中的元素[存的值]在数组中的顺序，从0开始计数，关联数组除外。
#
#数组取值练习
#  ${array[i]}  i表示元素的索引
#  使用@ 或 * 可以获取数组中的所有元素：
#  获取第一个元素
#  echo ${array[0]}
#  echo ${array[*]}			获取数组里的所有元素
#  echo ${#array[*]}			获取数组里所有元素个数
#  echo ${!array[@]}    	获取数组元素的索引索引
#  echo ${array[@]:1:2}    访问指定的元素；1代表从索引为1的元素开始获取；2代表获取后面几个元素
#五、关联数组
#  5.1 定义管理数组
#  关联数组使用首先需要申明该数组为关联数组，申明方式： declare -A 数组名称
#
#首先声明关联数组
declare -A asso_array1
declare -A asso_array2
declare -A asso_array3
#  5.2关联数组赋值
#  一次赋一个值
#  数组名[索引]=变量值
asso_array1[linux]=one
asso_array1[java]=two
asso_array1[php]=three
#  一次附多个值
asso_array2=([name1]=harry [name2]=jack [name3]=amy [name4]="Miss zhang")
#  查看关联数组
declare -A

#  管理数组取值
echo ${asso_array1[linux]}
echo ${asso_array1[php]}
echo ${asso_array1[*]}
echo ${!asso_array1[*]}
echo ${#asso_array1[*]}
echo ${#asso_array2[*]}
echo ${!asso_array2[*]}