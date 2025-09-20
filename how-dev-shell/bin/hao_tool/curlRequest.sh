#!/bin/bash
<<INFO
SCRIPT:curlRequest.sh
AUTHOR:haozhifeng
DATE:2025-09-20
DESCRIBE: 通过执行脚本，指定参数，发送curl请求，并将响应结果输出到控制台。
具体来说，该脚本接受两个命名参数：
--processNo：处理批次号
--userDepositAmount：用户充值金额，格式为“userId1:amount1,userId2:amount2,...”，其中userId是用户ID，amount是充值金额。
该脚本将根据这些参数构造一个JSON请求，并通过curl命令发送到服务器。
该脚本将输出curl的响应结果，并可选地将其保存到文件中。
其中，JSON 请求的格式如下：
{
    "clientId": "shell-client",
    "requestTime": 1758370225,
    "ip": "192.168.0.13",
    "data": {
        "processBatchNo": "123",
        "depositRequests": [
            {
                "userId": "10001",
                "amount": 1000
            },
            {
                "userId": "10002",
                "amount": 500
            }
        ]
    }
}

（1）不带参数运行脚本（显示用法提示）：
  ./curlRequest
  输出示例：
  Error: Both --processBatchNo and --userDepositAmount are required.
  Usage: curlRequest.sh --processBatchNo value1 --userDepositAmount value2
（2）带命名参数运行脚本：
  sh curlRequest.sh --processBatchNo "123" --userDepositAmount "10001:1000,10002:500"
  输出示例：
  [2025-09-20 10:00:00][PID:12345]@192.168.1.100 - processBatchNo: 123, userDepositAmount: 10001:1000,10002:500
  ......

  !!!注意，在测试脚本完整功能时，请启动how-dev-test-web项目服务，并确保服务端口号为8080。否则修改脚本中的SERVER_IP和SERVER_PORT的值为实际的服务IP和端口号。
INFO

SERVER_IP="127.0.0.1"
SERVER_PORT="8080"

# 获取当前时间
NOW_TIME=$(date +'%Y-%m-%d %H:%M:%S')
# 获取当前时间戳
NOW_TIME_STAMP=$(date +%s)

# 获取本机IP
#Linux
#OPS_LOCAP_IP=$(ifconfig eth1|grep 'inet '|awk '{print $2}')
#Mac
OPS_LOCAP_IP=$(ifconfig en0 | grep 'inet ' | awk '{print $2}')

# 解析命名参数
processBatchNo=""
userDepositAmount=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --processBatchNo)
            processBatchNo="$2"
            shift 2
            ;;
        --userDepositAmount)
            userDepositAmount="$2"
            shift 2
            ;;
        *)
            echo "Usage: $0 --processBatchNo value1 --userDepositAmount value2"
            exit 1
            ;;
    esac
done

# 检查参数是否为空
if [ -z "$processBatchNo" ] || [ -z "$userDepositAmount" ]; then
    echo "Error: Both --processBatchNo and --userDepositAmount are required."
    echo "Usage: $0 --processBatchNo value1 --userDepositAmount value2"
    exit 1
fi

# 输出传入的参数
echo "[${NOW_TIME}][PID:$$]@${OPS_LOCAP_IP} - processBatchNo: $processBatchNo, userDepositAmount: $userDepositAmount"

# 解析命令行参数
user_deposit_amount_param_value=$userDepositAmount
process_batch_no=$processBatchNo

# 将逗号分隔的字符串分割成数组
IFS=',' read -ra PAIRS <<< "${user_deposit_amount_param_value}"

user_deposit_amount_array="["
for pair in "${PAIRS[@]}"; do
    # 使用冒号分割每对值
    IFS=':' read -r user_id deposit_amount <<< "${pair}"

    # 将分割后的值添加到JSON数组中
    user_deposit_amount_array+="{\"userId\":\"${user_id}\",\"amount\":${deposit_amount}},"
done

# 去掉最后一个逗号
user_deposit_amount_array=${user_deposit_amount_array%,}
user_deposit_amount_array+="]"  # 结束JSON数组

# 输出JSON数组
echo "${user_deposit_amount_array}"

REQUEST_JSON=$(cat <<EOF
{
  "clientId": "shell-client",
  "requestTime": ${NOW_TIME_STAMP},
  "ip": "${OPS_LOCAP_IP}",
  "data": {
    "processBatchNo": "${process_batch_no}",
    "depositRequests": ${user_deposit_amount_array}
  }
}
EOF
)

# 打印构造的JSON
echo "构造的请求JSON:"
echo "${REQUEST_JSON}"

# 执行curl请求并捕获结果
RESPONSE=$(curl -X POST http://$SERVER_IP:$SERVER_PORT/batchDeposit \
  -H "Content-Type: application/json" \
  -d "${REQUEST_JSON}" \
  2>&1)

# 输出curl的响应结果
echo "curl响应结果:"
echo "$RESPONSE"

# 可选：将响应结果保存到文件
#echo "$RESPONSE" > curl_response.json
#echo "响应结果已保存到 curl_response.json"