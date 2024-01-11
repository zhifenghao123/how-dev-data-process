#!/usr/bin/env sh
<<INFO
SCRIPT:shell_12_regx.sh
AUTHOR:haozhifeng
DATE:2024-01-09
DESCRIBE:流程控制-正则表达式
INFO

#  1、正则表达式介绍
#  正则表达式（Regular Expression、regex或regexp，缩写为RE），也译为正规表示法、常规表示法，是一种字符模式，用于在查找过程中匹配指定的字符。
#  许多程序设计语言都支持利用正则表达式进行字符串操作。例如，在Perl中就内建了一个功能强大的正则表达式引擎。
#  正则表达式这个概念最初是由Unix中的工具软件（例如sed和grep）普及开的。
#  支持正则表达式的程序如：locate |find| vim| grep| sed |awk
#  正则表达式是一个三方产品，被常用计算机语言广泛使用，比如：shell、PHP、python、java、js等！

#  2、正则表达式特殊字符
#  定位符使用技巧：同时锚定开头和结尾，做精确匹配；单一锚定开头或结尾或者不锚定的，做模糊匹配。
#
#  定位符	说明
#  ^	    锚定开头 ^a 以a开头 默认锚定一个字符
#  $	    锚定结尾 a$ 以a结尾 默认锚定一个字符
#  1）精确匹配  以a开头c结尾的字符串
#  [root@haozhifeng ~]# egrep "^ac$" file
#
#  2）模糊匹配  以a开头
#  [root@haozhifeng ~]# egrep "^a" file

#  匹配符:匹配字符串
#  匹配符	  说明
#  .	      匹配除回车以外的任意一个字符
#  ( )	    字符串分组
#  [ ]	    定义字符类，匹配括号中的一个字符
#  [ ^ ]	  表示否定括号中出现字符类中的字符,取反。
#  \	      转义字符
#  |		    或

#  1）精确匹配  以a开头c结尾  中间任意  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a.c$" file
#
#  2）模糊匹配  以cc结尾的字符串   因为$只能锚定单个字符，如果是一个字符串就需要用()来做定义
#  [root@haozhifeng ~]# egrep "(cc)$" file
#
#  3）精确匹配  以a开头c结尾  中间是a-z,0-9  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a[a-z0-9]c$" file
#
#  4)精确匹配  以a开头c结尾  中间不包含a-z,0-9  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a[^a-z0-9]c$" file
#
#  5）精确匹配  以e开头f结尾  中间是*号  长度为三个字节的字符串  e*f
#  [root@haozhifeng ~]# egrep "^e\*f$" file
#
#  6）精确匹配 以a开头b或c结尾  中间是任意  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a.(b|c)$" file

#  限定符:对前面的字符或者(字符串)出现的次数做限定说明
#  限定符	  说明
#  *	      某个字符之后加星号表示该字符不出现或出现多次 a* (ab)*
#  ？	    与星号相似，但略有变化，表示该字符出现一次或不出现
#  +	      与星号相似，表示其前面字符出现一次或多次，但必须出现一次
#  {n,m}	  某个字符之后出现，表示该字符最少n次，最多m次
#  {m}	    正好出现了m次
#
#  1）精确匹配 以a开头 c结尾 中间是有b或者没有b 长度不限的字符串
#  [root@haozhifeng ~]# egrep "^ab*c$" file
#
#  2）精确匹配 以a开头 c结尾 中间只出现一次b或者没有b的字符串
#  [root@haozhifeng ~]# egrep "^ab?c$" file
#
#  3）精确匹配 以a开头 c结尾 中间是有b且至少出现一次 长度不限的字符串
#  [root@haozhifeng ~]# egrep "^ab+c$" file
#
#  4）精确匹配 以a开头 c结尾 中间是有b且至少出现两次最多出现四次 长度不限的字符串
#  [root@haozhifeng ~]# egrep "^ab{2,4}c$" file
#
#  5）精确匹配 以a开头 c结尾 中间是有b且正好出现三次的字符串
#  [root@haozhifeng ~]# egrep "^ab{3}c$" file
#
#  6) 精确匹配 以a开头 c结尾 中间是有b且至少出现一次的字符串
#  [root@haozhifeng ~]# egrep "^ab{1,}c$" file

#  3、正则表达式POSIX字符
#  posix字符一次只匹配一个范围中的一个字节
#
#  特殊字符	说明
#  [:alnum:]	匹配任意字母字符0-9 a-z A-Z
#  [:alpha:]	匹配任意字母，大写或小写
#  [:digit:]	数字 0-9
#  [:graph:]	非空字符( 非空格控制字符)
#  [:lower:]	小写字符a-z
#  [:upper:]	大写字符A-Z
#  [:cntrl:]	控制字符
#  [:print:]	非空字符( 包括空格)
#  [:punct:]	标点符号
#  [:blank:]	空格和TAB字符
#  [:xdigit:]	16 进制数字
#  [:space:]	所有空白字符( 新行、空格、制表符)
#  测试案例
#
#  注意[[ ]]  双中括号的意思:  第一个中括号是匹配符[] 匹配中括号中的任意一个字符，第二个[]是格式 如[:digit:]
#
#  1）精确匹配  以a开头c结尾  中间a-zA-Z0-9任意字符  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a[[:alnum:]]c$" file
#
#  2）精确匹配  以a开头c结尾  中间是a-zA-Z任意字符  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a[[:alpha:]]c$" file
#
#  3）精确匹配  以a开头c结尾  中间是0-9任意字符  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a[[:digit:]]c$" file
#
#  4）精确匹配  以a开头c结尾  中间是a-z任意字符  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a[[:lower:]]c$" file
#
#  4）精确匹配  以a开头c结尾  中间是A-Z任意字符  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a[[:upper:]]c$" file
#
#  5）精确匹配  以a开头c结尾  中间是非空任意字符  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a[[:print:]]c$" file
#
#  6）精确匹配  以a开头c结尾  中间是符号字符  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a[[:punct:]]c$" file
#
#  7）精确匹配  以a开头c结尾  中间是空格或者TAB符字符  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a[[:blank:]]c$" file
#
#  类似
#  [root@haozhifeng ~]# egrep "^a[[:space:]]c$" file
#
#  8）精确匹配  以a开头c结尾  中间是十六进制字符  长度为三个字节的字符串
#  [root@haozhifeng ~]# egrep "^a[[:xdigit:]]c$" file
#
#  说明：特殊字符和POSIX字符是两套字符，都可以完成需要的匹配，大家学习的时候最少要记住一套字符并熟练应用。
