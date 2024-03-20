#!/bin/bash
<<INFO
SCRIPT:cleanUp.sh
AUTHOR:haozhifeng
DATE:2024-03-19
DESCRIBE: 清理脚本。在/home/hzf/jenkins/workspace目录及其子目录中，查找所有修改时间超过1天的目录，并递归地、强制地、详细地删除它们。
INFO

# 1）find "/home/work/jenkins/workspace": 这部分告诉find命令从/home/work/jenkins/workspace这个目录开始搜索。
# 2）-type d: 这部分是一个条件，告诉find命令只查找目录（directory），而不查找文件。
# 3）-mtime +1: 这是另一个条件，它指定只查找那些修改时间（mtime）超过1天的目录。+1意味着“超过1天”。
# 4）-exec rm -rvf {} +: 这部分告诉find命令对找到的每个目录执行rm -rvf命令。
#   rm: 是一个用于删除文件或目录的命令。
#   -r: 递归删除目录及其内容。
#   -v: 显示详细的操作信息，即删除哪些目录和文件。
#   -f: 强制删除，不询问用户确认。
#   {}: 是一个占位符，代表find命令找到的每个目录。
#   +: 告诉find命令将尽可能多的目录名作为参数传递给rm命令，以提高效率。
find "/home/hzf/jenkins/workspace" -type d -mtime +1 -exec rm -rvf {} +

