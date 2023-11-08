#!/bin/sh

#写入数据对应的日期分片
dt=$1

function sendExeResultMessage() {
    echo "sendMessage"
}

function syncToOnlineMysql() {
    echo "syncToOnlineMysql, date:$dt"
}

function main() {
    echo "main start"
    syncToOnlineMysql $dt
    sendExeResultMessage
    echo "main end"
}

main