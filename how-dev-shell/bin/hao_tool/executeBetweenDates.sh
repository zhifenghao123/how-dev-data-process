#!/bin/bash
<<INFO
SCRIPT:shellDateTime.sh
AUTHOR:haozhifeng
DATE:2023-11-01
DESCRIBE:根据指定的两个日期，生成两个日期日期之间的所有日期，且格式为yyyyMMdd
INFO

function execute_between_dates() {
  # 定义起始日期和结束日期
  start_date=$1
  end_date=$2
  # 打印起始日期和结束日期
  echo "起始日期：$start_date"
  echo "结束日期：$end_date"

  # 循环打印日期
  current_date=$(date -d "$start_date" +%Y%m%d)
  end_date=$(date -d "$end_date" +%Y%m%d)
  while [ "$current_date" -le "$end_date" ]; do
    echo "'$current_date'"
    current_date=$(date -d "$current_date + 1 day" +%Y%m%d)
  done
}

# 调用函数并传递输入起始日期和结束日期
execute_between_dates "20230301" "20230331"