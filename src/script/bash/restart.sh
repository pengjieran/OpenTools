#!/bin/bash

tomcatbin=`pwd`
tomcat=${tomcatbin%%/bin*}
echo $tomcat

./shutdown.sh

rm -rf logs/* && rm -rf work/* && rm -rf conf/Catalina
echo "删除缓存文件完成"

pid=`ps aux|grep "java"|grep "$tomcat"|awk '{printf $2}'`

echo $pid
kill -9 $pid
echo "stop tomcat finished"
./startup.sh
tail -f ../logs/catalina.out

