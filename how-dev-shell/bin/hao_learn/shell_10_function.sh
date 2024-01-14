#!/usr/bin/env sh
<<INFO
SCRIPT:shell_10_function.sh
AUTHOR:haozhifeng
DATE:2024-01-09
DESCRIBE:shell 函数
INFO

#1.1、函数
#  shell中允许将一组命令集合或语句形成一段可用代码，这些代码块称为shell函数。给这段代码起个名字称为函数名，后续可以直接调用该段代码的功能。
#1.2、函数定义
#  语法一:
#  函数名 () {
#      代码块
#      return N
#      }
#
#
#  语法二：
#  function 函数名 {
#        代码块
#        return N
#        }
#
#
#  函数中return说明：
#  1.return可以结束一个函数，类似于前面讲的循环控制语句break(结束当前循环，执行循环体后面的代码)
#  2.return默认返回函数中最后一个命令的退出状态，也可以给定参数值，该参数值的范围是0-256之间。
#  3.如果没有return命令，函数将返回最后一个Shell的退出值。

#1.3、函数调用
#(1) 当前命令行调用
#  [root@haozhifeng shell04]# cat fun1.sh
#  #!/bin/bash
#  hello(){
#  echo "hello haozhifeng $1"
#  hostname
#  }
#  menu(){
#  cat <<-EOF
#  1. mysql
#  2. web
#  3. app
#  4. exit
#  EOF
#  }
#
#  [root@haozhifeng shell04]# source fun1.sh
#  [root@haozhifeng shell04]# . fun1.sh
#
#  [root@haozhifeng shell04]# hello 888
#  hello haozhifeng 888
#  haozhifeng
#  [root@haozhifeng shell04]# menu
#  1. mysql
#  2. web
#  3. app
#  4. exit
#
#
#（2）定义到用户的环境变量中
#  /etc/profile	/etc/bashrc		~/.bash_profile	~/.bashrc
#
#  [root@haozhifeng shell04]# cat ~/.bashrc
#  # .bashrc
#
#  # User specific aliases and functions
#
#  alias rm='rm -i'
#  alias cp='cp -i'
#  alias mv='mv -i'
#  # Source global definitions
#  if [ -f /etc/bashrc ]; then
#    . /etc/bashrc
#  fi
#
#  hello(){
#  echo "hello haozhifeng $1"
#  hostname
#  }
#  menu(){
#  cat <<-EOF
#  1. mysql
#  2. web
#  3. app
#  4. exit
#  EOF
#  }
#
#  注意：
#  当用户打开bash的时候会读取该文件
#
#（3）脚本中调用
#  #!/bin/bash
#  #打印菜单
#  source ./fun1.sh
#  menu(){
#  cat <<-END
#    h	显示命令帮助
#    f	显示磁盘分区
#    d	显示磁盘挂载
#    m	查看内存使用
#    u	查看系统负载
#    q	退出程序
#    END
#  }
#  menu		//调用函数



