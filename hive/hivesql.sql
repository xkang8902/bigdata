#1、获取数据源
#后台的服务数据
#前台的点击流日志数据
#业务数据
 
#2、确定主题
#用户主题：用户、会员相关的信息
#订单主题：订单相关的信息
#浏览器主题：跟浏览器相关的信息
#事件主题：跟事件相关
 
#3、创建模型（创建表）
#100张表以内不分层，以外将分层。
#如果维度表较多则将维度表单独分层
 
#1、创建维度层：
#dim_维度表
#地域维度：拆分成 省 市
#浏览器维度：浏览器名称、版本
#时间维度：周、季度
#事件维度：category(种类)、action
#平台维度：
#kpi：（指标）
 
#创建ods库：注：ods=operate database store
#创建dw库：dw=data warehouse 数据仓库
#创建dm库： dm=date manipulation数据操作
 
#命名规则：
#库名_主题_表名
#ods_user_addr_
#ODS_USER_ADDR_(大小写均可，但最好小写)
#ods.user.addr不行
 
#事实表
create database if not exists dim;//纬度
create database if not exists ods;//操作数据库存储
create database if not exists dw;//数据仓库
create database if not exists dm;//数据操作
 
#在维度库下面创建维度表：注：``字段带有特殊符号用这个解决,这里desc是关键字，所以用``
CREATE TABLE IF NOT EXISTS `dim_province` (
  `id` int,
  `province` string,
  `country_id` int,
  `desc` string
) 
row format delimited fields terminated by '\t'
;
 
CREATE TABLE IF NOT EXISTS `dim_city` (
  `id` int,
  `city` string,
  `desc` string
) 
row format delimited fields terminated by '\t'
;
 
CREATE TABLE IF NOT EXISTS `dim_province_city` (
  `dim_region_id` bigint,
  `dim_region_city_name` string,
  `dim_region_province_name` string,
  `dim_region_country_name` string,
  `dim_region_city_id` string,
  `dim_region_province_id` string,
  `dim_region_country_id` string,
  `dim_region_date` string
)
row format delimited fields terminated by '\t'
;
 
 
CREATE TABLE IF NOT EXISTS `dim_platform` (
  `id` int,
  `platform_name` string
)
row format delimited fields terminated by '\t'
;
 
 
 
CREATE TABLE IF NOT EXISTS `dim_kpi` (
  `id` int,
  `kpi_name` string
)
row format delimited fields terminated by '\t'
;
 
 
 
CREATE TABLE IF NOT EXISTS `dim_event_name` (
  `id` int,
  `name` string
)
row format delimited fields terminated by '\t'
;
 
CREATE TABLE IF NOT EXISTS `dim_event_category` (
  `id` int,
  `category` string
)
row format delimited fields terminated by '\t'
;
 
 
CREATE TABLE IF NOT EXISTS `dim_event_action` (
  `id` int,
  `action` string
)
row format delimited fields terminated by '\t'
;
 
 
CREATE TABLE IF NOT EXISTS `dim_browser_name` (
  `id` int,
  `browser_name` string,
  `browser_version_id` int
  )
row format delimited fields terminated by '\t'
;
 
CREATE TABLE IF NOT EXISTS `dim_browser_version` (
  `id` int,
  `browser_version` string
  )
row format delimited fields terminated by '\t'
;
 
CREATE TABLE IF NOT EXISTS `dim_order` (
  `oid` bigint,
  `on` string,
  `cut_id` bigint,
  `cua_id` bigint,
  `browser_version` string
  )
row format delimited fields terminated by '\t'
;
 
CREATE TABLE IF NOT EXISTS `dim_currency_type` (
  `id` int,
  `currency_name` string
)
row format delimited fields terminated by '\t'
;
 
CREATE TABLE IF NOT EXISTS `dim_payment_type` (
  `id` int,
  `payment_type` string
)
row format delimited fields terminated by '\t'
;
 
CREATE TABLE IF NOT EXISTS `dim_week`(
   STD_WEEK_CODE STRING,
   STD_WEEK_NAME STRING,
   BEGIN_DATE STRING,
   END_DATE STRING,
   NOTES STRING,
   IS_DISPLAY INT,
   DISPLAY_ORDER INT,
   IS_VALID INT,
   UPDATE_DATE STRING,
   LAST_STD_WEEK_CODE STRING
)              
row format delimited fields terminated by '\t'
;
 
 
 
CREATE TABLE IF NOT EXISTS `dim_userinfo` (
  `uid` String,
  `uname` string
)
row format delimited fields terminated by '\t'
;
 
 
 
load data local inpath '/root/dim/dim_province' into table dim_province;
load data local inpath '/root/dim/dim_city' into table dim_city;
load data local inpath '/root/dim/dim_province_city' into table dim_province_city;
load data local inpath '/root/dim/dim_platform' into table dim_platform;
load data local inpath '/root/dim/dim_kpi' into table dim_kpi;
load data local inpath '/root/dim/dim_event_name' into table dim_event_name;
load data local inpath '/root/dim/dim_browser_name' into table dim_browser_name;
load data local inpath '/root/dim/dim_browser_version' into table dim_browser_version;
load data local inpath '/root/dim/dim_userinfo' into table dim_userinfo;
 
#---------------------------------------------------
#在ods层创建数据表：这里用到对元数据进行处理(UDF)
create table if not exists ods_font_log(
ip string,
ts string,
server_ip string,
url string
)
row format delimited fields terminated by '\u0001'
stored as textfile
;
 
create table if not exists ods_end_log(
ip string,
ts string,
server_ip string,
url string
)
row format delimited fields terminated by '\u0001'
stored as textfile
;
 
 
create table if not exists ods_logs(
ip string,
ts string,
server_ip string,
url string
)
row format delimited fields terminated by '\t'
stored as orc
;
 
insert into ods_logs
select 
from ods_font_log
union all
select 
from ods_end_log
;
 
#udf:
#ip
#url解析
#或者用mr清洗数据：
#--------------------------------------------------
create table if not exists ods_logs(
ver string,
s_time string,
en string,
u_ud string,
u_mid string,
u_sd string,
c_time string,
l string,
b_iev string,
b_rst string,
p_url string,
p_ref string,
tt string,
pl string,
ip string,
oid string,
`on` string,
cua string,
cut string,
pt string,
ca string,
ac string,
kv_ string,
du string,
browser_name string,
browser_version string,
os_name string,
os_version string,
country string,
province string,
city string
)
row format delimited fields terminated by '\u0001'
stored as textfile
;
#注：
#删除内部表会直接删除元数据（metadata）及存储数据；
#删除外部表仅仅会删除元数据，HDFS上的文件并不会被删除； 
 
create external table if not exists ods_logs_orc(
ver string,
s_time string,
en string,
u_ud string,
u_mid string,
u_sd string,
c_time string,
l string,
b_iev string,
b_rst string,
p_url string,
p_ref string,
tt string,
pl string,
ip string,
oid string,
`on` string,
cua string,
cut string,
pt string,
ca string,
ac string,
kv_ string,
du string,
browser_name string,
browser_version string,
os_name string,
os_version string,
country string,
province string,
city string
)
partitioned by (month string,day string)
row format delimited fields terminated by '\u0001'
stored as orc
;
 
#导入原始数据：
#load data inpath '/ods/11/09/' into table ods_logs;
#我因为没跑mr程序所以没有这个数据，让同学给了一份：/root/dim/part-m-00000
load data local inpath '/root/dim/part-m-00000' into table ods_logs;
 
#将数据导入到orc表中
#-----------------------
from ods_logs
insert into ods_logs_orc(
ver ,
s_time ,
en ,
u_ud ,
u_mid ,
u_sd ,
c_time ,
l ,
b_iev ,
b_rst ,
p_url ,
p_ref ,
tt ,
pl ,
ip ,
oid ,
`on` ,
cua ,
cut ,
pt ,
ca ,
ac ,
kv_ ,
du ,
browser_name ,
browser_version ,
os_name ,
os_version ,
country ,
province ,
city
) partition (month=1,day='01')
select 
ver ,
s_time ,
en ,
u_ud ,
u_mid ,
u_sd ,
c_time ,
l ,
b_iev ,
b_rst ,
p_url ,
p_ref ,
tt ,
pl ,
ip ,
oid ,
`on` ,
cua ,
cut ,
pt ,
ca ,
ac ,
kv_ ,
du ,
browser_name ,
browser_version ,
os_name ,
os_version ,
country ,
province ,
city
;
#------------------
#上面的报错，可以直接简写为：
from ods_logs
insert into ods_logs_orc partition(month=1,day=1)
select *
;
 
 
#创建dw层的数据
##为新增用户、总用户、活跃用户做计算：
create table if not exists dwd_user(
pl string,
pl_id string,
en string,
en_id string,
browser_name string,
browser_id string,
browser_version string,
browser_version_id string,
province_name string,
province_id string,
city_name string,
city_id string,
uid string
)
partitioned by (month string,day string)
row format delimited fields terminated by '\u0001'
stored as orc
;
 
#导入数据：
from (
select
ol.pl,
dp.id as pl_id,
ol.en,
de.id as en_id,
ol.browser_name,
db.id browser_id,
ol.browser_version ,
dv.id browser_version_id,
ol.province ,
pv.id province_id,
ol.city ,
dc.id city_id,
ol.u_ud
from ods.ods_logs_orc ol
left join dim.dim_platform dp on dp.platform_name = ol.pl
left join dim.dim_event_name de on de.name = ol.en
left join dim.dim_browser_name db on db.browser_name = ol.browser_name
left join dim.dim_browser_version dv on dv.browser_version = ol.browser_version
left join dim.dim_province pv on pv.province = ol.province
left join dim.dim_city dc on dc.city = ol.city
) tmp
insert into dwd_user partition (month='1',day = '01')
select *
;
 
#-----------------------
#另一种写法：
with tmp as (
select
ol.pl,
dp.id as pl_id,
ol.en,
de.id as en_id,
ol.browser_name,
db.id browser_id,
ol.browser_version ,
dv.id browser_version_id,
ol.province_name ,
pv.id province_id,
ol.city_name ,
dc.id city_id,
ol.uid
from ods_logs_orc ol
left join dim_platform dp on dp.platform_name = ol.pl
left join dim_event_name de on de.name = ol.en
left join dim_browser_name db on db.browser_name = ol.browser_Name
left join dim_browser_version dv on dv.browser_version = ol.browser_version
left join dim_province pv on pv.province = ol.province_name
left join dim_city dc on dc.city = ol.city_name
) 
insert into dwd_user partition (month='1',day = '01')
select * from tmp
;
#创建dw层的数据
###为新增用户、总用户、活跃用户做计算：
create table if not exists dwd_user(
pl string,
pl_id string,
en string,
en_id string,
browser_name string,
browser_id string,
browser_version string,
browser_version_id string,
province_name string,
province_id string,
city_name string,
city_id string,
uid string
)
partitioned by (month string,day string)
row format delimited fields terminated by '\u0001'
stored as orc
;
 
#导入数据：
from (
select
ol.pl,
dp.id as pl_id,
ol.en,
de.id as en_id,
ol.browser_name,
db.id browser_id,
ol.browser_version ,
dv.id browser_version_id,
ol.province ,
pv.id province_id,
ol.city ,
dc.id city_id,
ol.u_ud
from ods.ods_logs_orc ol
left join dim.dim_platform dp on dp.platform_name = ol.pl
left join dim.dim_event_name de on de.name = ol.en
left join dim.dim_browser_name db on db.browser_name = ol.browser_name
left join dim.dim_browser_version dv on dv.browser_version = ol.browser_version
left join dim.dim_province pv on pv.province = ol.province
left join dim.dim_city dc on dc.city = ol.city
) tmp
insert into dwd_user partition (month='1',day = '01')
select *
;
 
#-------------------------------
 
#创建dm层的数据：
##用户主题下新增用户、新增总用户、日活跃用户
create table if not exists dm_user_users(
pl string,
pl_id string,
new_user_count int,
new_total_user_count int,
active_user_count int
)
partitioned by (month string,day string)
row format delimited fields terminated by '\u0001'
stored as orc
;
 
#导入数据：
from (
select 
us.pl,
us.pl_id,
count(distinct us.uid) as new_user_count,
count(distinct us.uid) + nvl(uu.new_total_user_count,0) as new_total_user_count,
0 as active_user_count
from dwd.dwd_user us
left join dm_user_users uu on uu.month = '1' and uu.day = '00' and us.pl_id = uu.pl_id
where us.month = 1 and us.day = '01' and us.en_id = 1
group by us.pl,us.pl_id,uu.new_total_user_count
union all select 
us.pl,
us.pl_id,
0 as new_user_count,
0 as new_total_user_count,
count(distinct us.uid) as active_user_count
from dwd.dwd_user us
where us.month = 1 and us.day = '01'
group by us.pl,us.pl_id)tmp
 
insert into dm_user_users partition (month = 1,day ='01')
 
select tmp.pl,tmp.pl_id,
sum(tmp.new_user_count) new_user_count,
sum(tmp.new_total_user_count) new_total_user_count,
sum(tmp.active_user_count) active_user_count
 
group by tmp.pl,tmp.pl_id
;
#注：dwd_user.en_id = 1 表示lunch事件
#输出：
dm_user_users.pl        dm_user_users.pl_id     dm_user_users.new_user_count    dm_user_users.new_total_user_count  dm_user_users.active_user_count  dm_user_users.month     dm_user_users.day
java    NULL    0       0       1       1       01
java_server     1       0       0       2       1       01
website 2       5       5       8       1       01
 
 
#浏览器模块下的新增用户、新增总用户、日活跃用户
create table if not exists dm_browser_users(
pl string,
pl_id string,
browser_name string,
browser_id string,
browser_version string,
browser_version_id string,
new_user_count int,
new_total_user_count int,
active_user_count int
)
partitioned by (month string,day string)
row format delimited fields terminated by '\u0001'
stored as orc
;
#分析：平台和浏览器有关，因为有平台相关字段
#browser_version_id和浏览器类型无关
from (
select 
us.pl,
us.pl_id,
us.browser_name,
us.browser_id,
us.browser_version,
us.browser_version_id,
count(distinct us.uid) as new_user_count,
count(distinct us.uid) + nvl(bu.new_total_user_count,0) as new_total_user_count,
0 as active_user_count
from dwd.dwd_user us
left join dm_browser_users bu on bu.month = '1' and bu.day = '00' and bu.pl_id=us.pl_id and 
bu.browser_id = us.browser_id and 
bu.browser_version_id = us.browser_version_id 
where us.month = 1 and us.day = '01' and us.en_id =1
group by us.pl,us.pl_id,us.browser_name,us.browser_id,us.browser_version,us.browser_version_id,bu.new_total_user_count
union all select 
us.pl,
us.pl_id,
us.browser_name,
us.browser_id,
us.browser_version,
us.browser_version_id,
0 as new_user_count,
0 as new_total_user_count,
count(distinct us.uid) as active_user_count
from dwd.dwd_user us
where us.month = 1 and us.day = '01'
group by us.pl,us.pl_id,us.browser_name,us.browser_id,us.browser_version,us.browser_version_id)tmp
insert into dm_browser_users partition (month = 1,day ='01')
select 
tmp.pl,
tmp.pl_id,
tmp.browser_name,
tmp.browser_id,
tmp.browser_version,
tmp.browser_version_id,
sum(tmp.new_user_count) new_user_count,
sum(tmp.new_total_user_count) new_total_user_count,
sum(tmp.active_user_count) active_user_count
group by tmp.pl,tmp.pl_id,tmp.browser_name,tmp.browser_id,tmp.browser_version,tmp.browser_version_id;
输出：
dm_browser_users.pl     dm_browser_users.pl_id  dm_browser_users.browser_name   dm_browser_users.browser_id     dm_browser_users.browser_version     dm_browser_users.browser_version_id     dm_browser_users.new_user_count dm_browser_us                                     ers.new_total_user_count        dm_browser_users.active_user_count      dm_browser_users.month  dm_browser_users.day
java    NULL    Chrome  4       31.0.1650.63    4       0       0       1       1       01
java_server     1       Chrome  4       31.0.1650.63    4       0       0       1       1       01
java_server     1       null    3       null    3       0       0       1       1       01
website 2       Chrome  4       31.0.1650.63    4       0       0       1       1       01
website 2       Chrome  4       47.0.2526.106   NULL    1       1       1       1       01
website 2       Chrome  4       70.0.3538.77    NULL    1       1       2       1       01
website 2       Firefox NULL    63.0    NULL    0       0       1       1       01
website 2       IE      1       8.0     1       2       2       2       1       01
website 2       Sogou Explorer  2       2.X     2       1       1       1       1       01
Time taken: 0.177 seconds, Fetched: 9 row(s)
 
#地域主题下的活跃用户
create table if not exists dm_area_users(
pl string,
pl_id string,
province string,
province_id string,
city string,
city_id string,
active_user_count int
)
partitioned by (month string,day string)
row format delimited fields terminated by '\u0001'
stored as orc
;
from (
select 
us.pl,
us.pl_id,
us.province_name,
us.province_id,
us.city_name,
us.city_id,
count(distinct us.uid) as active_user
from dwd.dwd_user us
where us.month = 1 and us.day = '01'
group by us.pl,us.pl_id,us.province_name,us.province_id,us.city_name,us.city_id)tmp
insert into dm_area_users partition (month = 1,day ='01')
select 
tmp.pl,
tmp.pl_id,
tmp.province_name,
tmp.province_id,
tmp.city_name,
tmp.city_id,
tmp.active_user
group by tmp.pl,tmp.pl_id,tmp.province_name,tmp.province_id,tmp.city_name,tmp.city_id,tmp.active_user;
 
#输出：
hive (dm)> select * from dm_area_users;
OK
dm_area_users.pl        dm_area_users.pl_id     dm_area_users.province  dm_area_users.province_id       dm_area_users.city   dm_area_users.city_id   dm_area_users.active_user_count dm_area_users.month     dm_area_users.day
java    NULL    北京市  2002    昌平区  NULL    1       1       01
java_server     1       广西省  NULL    广西南宁市      NULL    1       1       01
java_server     1       贵州省  2025    贵阳市  4266    1       1       01
website 2       北京市  2002    昌平区  NULL    4       1       01
website 2       广西省  NULL    广西南宁市      NULL    1       1       01
website 2       河南省  2017    平顶山市        4109    1       1       01
website 2       贵州省  2025    贵阳市  4266    2       1       01
website 2       贵州省  2025    黔西南州兴义市  NULL    2       1       01

create table if not exists dwd_mem(
pl string,
pl_id string,
en string,
en_id string,
browser_name string,
browser_id string,
browser_version string,
browser_version_id string,
province_name string,
province_id string,
city_name string,
city_id string,
u_mid string
)
partitioned by (month string,day string)
row format delimited fields terminated by '\u0001'
stored as orc
;
 
from (
select
ol.pl,
dp.id as pl_id,
ol.en,
de.id as en_id,
ol.browser_name,
db.id browser_id,
ol.browser_version ,
dv.id browser_version_id,
ol.province ,
pv.id province_id,
ol.city ,
dc.id city_id,
ol.u_mid
from ods.ods_logs_orc ol
left join dim.dim_platform dp on dp.platform_name = ol.pl
left join dim.dim_event_name de on de.name = ol.en
left join dim.dim_browser_name db on db.browser_name = ol.browser_name
left join dim.dim_browser_version dv on dv.browser_version = ol.browser_version
left join dim.dim_province pv on pv.province = ol.province
left join dim.dim_city dc on dc.city = ol.city
) tmp
insert into dwd_mem partition (month='1',day = '01')
select *
;
 
1、会员主题下新增会员、总会员、活跃会员
 
创建dm层的数据：
#会员主题下新增用户、新增总用户、日活跃用户
create table if not exists dm_mem_mems(
pl string,
pl_id string,
new_mem_count int,
new_total_mem_count int,
active_mem_count int
)
partitioned by (month string,day string)
row format delimited fields terminated by '\u0001'
stored as orc
;
 
导入数据：
from (
select 
mem.pl,
mem.pl_id,
count(distinct mem.u_mid) as new_mem_count,
count(distinct mem.u_mid) + nvl(mm.new_total_mem_count,0) as new_total_mem_count,
0 as active_mem_count
from dwd.dwd_mem mem
left join dm_mem_mems mm on mm.month = '1' and mm.day = '00' and mm.pl_id = mem.pl_id
where mem.month = 1 and mem.day = '01' and mem.en_id = 1
group by mem.pl,mem.pl_id,mm.new_total_mem_count
union all select
mem.pl,
mem.pl_id,
0 as new_mem_count,
0 as new_total_mem_count,
count(distinct mem.u_mid) as active_mem_count
from dwd.dwd_mem mem
where mem.month = 1 and mem.day ='01'
group by mem.pl,mem.pl_id)tmp
insert into dm_mem_mems partition(month = 1,day ='01')
select tmp.pl,tmp.pl_id,
sum(tmp.new_mem_count) new_mem_count,
sum(tmp.new_total_mem_count) new_total_mem_count,
sum(tmp.active_mem_count) active_mem_count
group by tmp.pl,tmp.pl_id
;
输出
hive (dm)> select * from dm_user_users;
OK
dm_user_users.pl        dm_user_users.pl_id     dm_user_users.new_user_count    dm_user_users.new_total_user_count      dm_user_users.active_user_count  dm_user_users.month     dm_user_users.day
java    NULL    0       0       1       1       01
java_server     1       0       0       2       1       01
website 2       5       5       8       1       01
 
 
2、浏览器主题下新增会员、总会员、活跃会员
 
create table if not exists dm_browser_mems(
pl string,
pl_id string,
browser_name string,
browser_id string,
browser_version string,
browser_version_id string,
new_mem_count int,
new_total_mem_count int,
active_mem_count int
)
partitioned by (month string,day string)
row format delimited fields terminated by '\u0001'
stored as orc
;
 
#分析：平台和浏览器有关，因为有平台相关字段
#browser_version_id和浏览器类型无关
from (
select 
mem.pl,
mem.pl_id,
mem.browser_name,
mem.browser_id,
mem.browser_version,
mem.browser_version_id,
count(distinct mem.u_mid) as new_mem_count,
count(distinct mem.u_mid) + nvl(bm.new_total_mem_count,0) as new_total_mem_count,
0 as active_mem_count
from dwd.dwd_mem mem
left join dm_browser_mems bm on bm.month = '1' and bm.day = '00' and bm.pl_id=mem.pl_id and 
bm.browser_id = mem.browser_id and 
bm.browser_version_id = mem.browser_version_id 
where mem.month = 1 and mem.day = '01' and mem.en_id =1
group by mem.pl,mem.pl_id,mem.browser_name,mem.browser_id,mem.browser_version,mem.browser_version_id,bm.new_total_mem_count
union all select 
mem.pl,
mem.pl_id,
mem.browser_name,
mem.browser_id,
mem.browser_version,
mem.browser_version_id,
0 as new_mem_count,
0 as new_total_mem_count,
count(distinct mem.u_mid) as active_mem_count
from dwd.dwd_mem mem
where mem.month = 1 and mem.day = '01'
group by mem.pl,mem.pl_id,mem.browser_name,mem.browser_id,mem.browser_version,mem.browser_version_id)tmp
insert into dm_browser_mems partition (month = 1,day ='01')
select 
tmp.pl,
tmp.pl_id,
tmp.browser_name,
tmp.browser_id,
tmp.browser_version,
tmp.browser_version_id,
sum(tmp.new_mem_count) new_mem_count,
sum(tmp.new_total_mem_count) new_total_mem_count,
sum(tmp.active_mem_count) active_mem_count
group by tmp.pl,tmp.pl_id,tmp.browser_name,tmp.browser_id,tmp.browser_version,tmp.browser_version_id;
 
输出：
hive (dm)> select * from dm_browser_mems;
OK
dm_browser_mems.pl      dm_browser_mems.pl_id   dm_browser_mems.browser_name    dm_browser_mems.browser_id      dm_browser_mems.b                         rowser_version  dm_browser_mems.browser_version_id      dm_browser_mems.new_mem_count   dm_browser_mems.new_total_mem_count     d                         m_browser_mems.active_mem_count dm_browser_mems.month   dm_browser_mems.day
java    NULL    Chrome  4       31.0.1650.63    4       0       0       1       1       01
java_server     1       Chrome  4       31.0.1650.63    4       0       0       2       1       01
java_server     1       null    3       null    3       0       0       2       1       01
website 2       Chrome  4       31.0.1650.63    4       0       0       1       1       01
website 2       Chrome  4       47.0.2526.106   NULL    1       1       1       1       01
website 2       Chrome  4       70.0.3538.77    NULL    1       1       1       1       01
website 2       Firefox NULL    63.0    NULL    0       0       1       1       01
website 2       IE      1       8.0     1       1       1       1       1       01
website 2       Sogou Explorer  2       2.X     2       1       1       1       1       01
 
 
3、地域主题下的活跃会员
create table if not exists dm_area_mems(
pl string,
pl_id string,
province string,
province_id string,
city string,
city_id string,
active_user_count int
)
partitioned by (month string,day string)
row format delimited fields terminated by '\u0001'
stored as orc
;
 
from (
select 
mem.pl,
mem.pl_id,
mem.province_name,
mem.province_id,
mem.city_name,
mem.city_id,
count(distinct mem.u_mid) as active_mem
from dwd.dwd_mem mem
where mem.month = 1 and mem.day = '01'
group by mem.pl,mem.pl_id,mem.province_name,mem.province_id,mem.city_name,mem.city_id)tmp
insert into dm_area_mems partition (month = 1,day ='01')
select 
tmp.pl,
tmp.pl_id,
tmp.province_name,
tmp.province_id,
tmp.city_name,
tmp.city_id,
tmp.active_mem
group by tmp.pl,tmp.pl_id,tmp.province_name,tmp.province_id,tmp.city_name,tmp.city_id,tmp.active_mem;
 
输出：
hive (dm)> select * from dm_area_mems;
OK
dm_area_mems.pl dm_area_mems.pl_id      dm_area_mems.province   dm_area_mems.province_id        dm_area_mems.city       dm_area_mems.city_id     dm_area_mems.active_user_count  dm_area_mems.month      dm_area_mems.day
java    NULL    北京市  2002    昌平区  NULL    1       1       01
java_server     1       广西省  NULL    广西南宁市      NULL    2       1       01
java_server     1       贵州省  2025    贵阳市  4266    2       1       01
website 2       北京市  2002    昌平区  NULL    1       1       01
website 2       广西省  NULL    广西南宁市      NULL    1       1       01
website 2       河南省  2017    平顶山市        4109    1       1       01
website 2       贵州省  2025    贵阳市  4266    1       1       01
website 2       贵州省  2025    黔西南州兴义市  NULL    1       1       01

