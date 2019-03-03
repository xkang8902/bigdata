#hive向脚本传参
    function load2hive(){
        
        hive -hivevar path=$1 -hivevar table=$2 -f '/home/greenet/gnhadoop/hive_sql_dml2.sql'
        #hive -hiveconf path=$1 -hiveconf table=$2 -f '/home/greenet/gnhadoop/hive_sql_dml2.sql'
    
    }
    
    load2hive $radius_remote $radius_table
#    sql,注意table不需要引号

    use ctlan;
    load data local inpath '${hivevar:path}' into table ${hivevar:table};

