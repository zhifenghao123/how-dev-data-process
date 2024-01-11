#!/usr/bin/env sh
<<INFO
SCRIPT:shell_11_case.sh
AUTHOR:haozhifeng
DATE:2024-01-09
DESCRIBE:流程控制-case语句
INFO

#  1.1、case语法
#  case $var in             定义变量;var代表是变量名
#  pattern 1)              模式1;用 | 分割多个模式，相当于or
#      command1            需要执行的语句
#      ;;                  两个分号代表命令结束
#  pattern 2)
#      command2
#      ;;
#  pattern 3)
#      command3
#      ;;
#        *)              default，不满足以上模式，默认执行*)下面的语句
#      command4
#      ;;
#  esac							esac表示case语句结束

#写一个nginx启动管理脚本，可以实现/etc/init.d/nginx start|stop|restart|status|reload 或者 systemctl start nginx

#variables
nginx_install_doc=/usr/local/nginx
proc=nginx
nginxd=$nginx_install_doc/sbin/nginx
pid_file=$nginx_install_doc/logs/nginx.pid

# Source function library.
if [ -f /etc/init.d/functions ];then
   . /etc/init.d/functions
else
   echo "not found file /etc/init.d/functions"
   exit
fi

#假如pid文件存在，那么统计一下nginx进程数量
if [ -f $pid_file ];then
   nginx_process_id=`cat $pid_file`
   nginx_process_num=`ps aux |grep $nginx_process_id|grep -v "grep"|wc -l`
fi


#function
start () {
#如果nginx 没有启动直接启动，否则报错 已经启动
if [ -f $pid_file ]&&[ $nginx_process_num -ge 1 ];then
   echo "nginx running..."
else
   #如果pid文件存在，但是没有进程，说明上一次非法关闭了nginx,造成pid文件没有自动删除,所以启动nginx之前先删除旧的pid文件
   if [ -f $pid_file ] && [ $nginx_process_num -lt 1 ];then
        rm -f $pig_file
	#可以使用两个函数，两种方法来执行命令，并返回执行结果
        #1)daemon
        #2)action   建议这个，简单易用

	#echo " nginx start `daemon $nginxd` "
        action "nginx start" $nginxd
  fi
  #echo " nginx start `daemon $nginxd` "
  action "nginx start" $nginxd
fi

}

stop () {
#判断nginx启动的情况下才会执行关闭，如果没启动直接报错，或者提示用户服务没启动,这里我直接报错的原因是为了给大家演示失败的输出
if [ -f $pid_file ]&&[ $nginx_process_num -ge 1 ];then
     action "nginx stop" killall -s QUIT $proc
     rm -f $pid_file
else
     action "nginx stop" killall -s QUIT $proc 2>/dev/null
fi
}

restart () {
  stop
  sleep 1
  start
}

reload () {
#重载的目的是让主进程重新加载配置文件,但是前提是服务必须开启
#这里先判断服务是否开启，开启就执行加载，没有开启直接报加载错误
if [ -f $pid_file ]&&[ $nginx_process_num -ge 1 ];then
    action "nginx reload" killall -s HUP $proc
else
    action "nginx reload" killall -s HUP $proc 2>/dev/null
fi
}

status () {
if [ -f $pid_file ]&&[ $nginx_process_num -ge 1 ];then
 echo "nginx running..."
else
 echo "nginx stop"
fi
}

#callable
case $1 in
start) start;;
stop) stop;;
restart) restart;;
reload) reload;;
status) status;;
*) echo "USAGE: $0 start|stop|restart|reload|status";;
esac




