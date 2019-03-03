#hive脚本加载数据到hive分区表
    access_logs/20170610/2017061000.log
    2017061001.log
    2017061002.log
    ......
    2017061023.log

#二级分区：天/小时
    定时命令
   crontab -l 查看定时
   crontab -e 设置定时
   前5个字段分别表示： 
       分钟：0-59 
       小时：1-23 
       日期：1-31 
       月份：1-12 
       星期：0-6（0表示周日）
    crontab+shell 实现自动调度。
    #每5分钟执行一次命令
    crontab -e
    */5 * * * * . /etc/profile; /bin/sh /usr/bigdata/timersh/sqooptimer.sh

#建库：
    create database load_hive;

#建表：
    create table load_h(
    id              string,
    url             string,
    referer         string,
    keyword         string,
    type            string,
    guid            string,
    pageId          string,
    moduleId        string,
    linkId          string,
    attachedInfo    string,
    sessionId       string,
    trackerU        string,
    trackerType     string,
    ip              string,
    trackerSrc      string,
    cookie          string,
    orderCode       string,
    trackTime       string,
    endUserId       string,
    firstLink       string,
    sessionViewNo   string,
    productId       string,
    curMerchantId   string,
    provinceId      string,
    cityId          string,
    fee             string,
    edmActivity     string,
    edmEmail        string,
    edmJobId        string,
    ieVersion       string,
    platform        string,
    internalKeyword string,
    resultSum       string,
    currentPage     string,
    linkPosition    string,
    buttonPosition  string
    )
    partitioned by (date string,hour string)
    row format delimited fields terminated by '\t'
    stored as textfile;

#--hiveconf  指定参数
    key=value
    show partitions load_hive.load_h;  --查看表分区情况。


#通过Shell脚本及可执行SQL文件执行：load_to_hive_f.sh
    #!/bin/bash
    
    #load
    
    #define the date of yesterday
    YESTERDAY=`date -d '-1 days' +%Y%m%d`
    
    #define log dir
    ACCESS_LOGS_DIR=/opt/datas/access_logs/$YESTERDAY
    
    #define hive home
    HIVE_HOME=/opt/cdh5/hive-0.13.1-cdh5.3.6
    
    #load
    for FILE in `ls $ACCESS_LOGS_DIR`
    do
        Day=${FILE:0:8}
        Hour=${FILE:8:2}
        echo "${Day}+${Hour}"
        $HIVE_HOME/bin/hive --hiveconf log_dir=$ACCESS_LOGS_DIR --hiveconf file_path=$FILE --hiveconf DAY=$Day --hiveconf HOUR=$Hour -f /opt/datas/hive_script/load.sql
    done
    $HIVE_HOME/bin/hive -e "show partitions load_hive.load_h"


#SQL文件：load.sql
    load data local inpath '${hiveconf:log_dir}/${hiveconf:file_path}' into table load_hive.load_h partition(date='${hiveconf:DAY}',hour='${hiveconf:HOUR}');
