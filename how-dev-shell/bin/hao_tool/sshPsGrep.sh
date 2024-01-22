#!/bin/bash
<<INFO
SCRIPT:shellDateTime.sh
AUTHOR:haozhifeng
DATE:2023-11-01
DESCRIBE: 通过ssh 批量执行命令
INFO

HOSTS=(192.168.1.1 192.168.1.2 192.168.1.3 192.168.1.4)

for HOST in ${HOSTS[*]}
do
    echo "--------$HOST-------"
    #    ps -ef| grep java | grep -iv grep
    ssh -T $HOST << DELIMITER
    ps -ef| grep java | grep -vE "(grep|ps)"
    exit
DELIMITER
done