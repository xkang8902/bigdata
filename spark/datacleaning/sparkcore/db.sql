create table day_video_access_topn_stat(
day varchar (8) not null,
cms_id bigint(10) not null,
times bigint(10) not null,
primary key (day,cms_id)
);
 
create table day_video_city_access_topn_stat(
day varchar (8) not null,
cms_id bigint(10) not null,
city varchar (20) not null,
times bigint(10) not null,
times_rank int not null,
primary key (day,city,cms_id)
);
 
create table day_video_traffics_access_topn_stat(
day varchar (8) not null,
cms_id bigint(10) not null,
traffics bigint (10) not null,
primary key (day,cms_id)
);
