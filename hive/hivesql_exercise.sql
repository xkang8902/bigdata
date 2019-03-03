--------------------
student(s_id,s_name,s_birth,s_sex) –学生编号,学生姓名, 出生年月,学生性别 
teacher(t_id,t_name) –教师编号,教师姓名 

course(c_id,c_name,t_id) – –课程编号, 课程名称, 教师编号  课程编号：1语文 2数学 3英语
score(s_id,c_id,s_score) –学生编号,课程编号,分数
--------------------
【UNION ALL】
    select a,b,sum(sm) AS s1, SUM(qm) AS s2 from 
    (
    select 'a' AS a, 'b' AS b, 2 AS sm, 200 AS qm 
    UNION ALL
    select 'a' AS a, 'b' AS b, 2 AS sm, 200 AS qm
    )r
    group by a,b
    ab4400
【UNION】
    select a,b,sum(sm) AS s1, SUM(qm) AS s2 from 
    (
    select 'a' AS a, 'b' AS b, 2 AS sm, 200 AS qm 
    UNION 
    select 'a' AS a, 'b' AS b, 2 AS sm, 200 AS qm
    )r
    group by a,b
    ab2200

总结：UNION ALL全部列出，UNION会去掉重复的。

注：unoin all 的子句是不允许排序和limit的,但是字句中row_number()over(这里可以排序)
group by 多个字段：如group by name,number，我们可以把name和number 看成一个整体字段，是以他们整体来进行分组的。
substring(string A, int start, int len)
 
 
1、查询"01"课程比"02"课程成绩高的学生的信息及课程分数:
先找到一个学生的01成绩和02成绩
select * from score l join score r on l.s_id=r.s_id and l.c_id=1 and r.c_id=2; 
 
select * from 
(select l.s_id l_s_id,l.c_id l_c_id,l.s_score l_s_score,r.s_id r_s_id,r.c_id r_c_id,r.s_score r_s_score
from score l join score r on l.s_id=r.s_id where (l.c_id=1 and r.c_id=2) and l.s_score>r.s_score)t
left join student on t.l_s_id =student.s_id; 
输出：
t.l_s_id        t.l_c_id        t.l_s_score     t.r_s_id        t.r_c_id        t.r_s_score student.s_id     student.s_name  student.s_birth student.s_sex
2       1       70      2       2       60      2       钱电    1990-12-21      男
4       1       50      4       2       30      4       李云    1990-08-06      男
 
2、查询"01"课程比"02"课程成绩低的学生的信息及课程分数:
select * from 
(select l.s_id l_s_id,l.c_id l_c_id,l.s_score l_s_score,r.s_id r_s_id,r.c_id r_c_id,r.s_score r_s_score
from score l join score r on l.s_id=r.s_id where (l.c_id=1 and r.c_id=2) and l.s_score<r.s_score)t
left join student on t.l_s_id =student.s_id; 
输出：
t.l_s_id        t.l_c_id        t.l_s_score     t.r_s_id        t.r_c_id        t.r_s_score student.s_id     student.s_name  student.s_birth student.s_sex
1       1       80      1       2       90      1       赵雷    1990-01-01      男
5       1       76      5       2       87      5       周梅    1991-12-01      女
 
3、查询平均成绩大于等于60分的同学的学生编号和学生姓名和平均成绩:
select s_id,s_name,avg(s_score) fs from score join student on score.s_id=student.s_id group by s_name,score.s_id having fs>=60;
输出：
s_id    s_name  fs
5       周梅    81.5
3       孙风    80.0
1       赵雷    89.66666666666667
7       郑竹    93.5
2       钱电    70.0
 
4、查询平均成绩小于60分的同学的学生编号和学生姓名和平均成绩:
(包括有成绩的和无成绩的)
select s_id,s_name,avg(s_score) fs from score join student on score.s_id=student.s_id group by s_name,score.s_id having fs<=60;
 
5、查询所有同学的学生编号、学生姓名、选课总数、所有课程的总成绩:
select score.s_id,s_name,count(*),sum(s_score) from score 
join student on score.s_id=student.s_id group by score.s_id,s_name;
输出
1       赵雷    3       269
2       钱电    3       210
3       孙风    3       240
4       李云    3       100
5       周梅    2       163
6       吴兰    2       65
7       郑竹    2       187
 
6、查询"李"姓老师的数量:
select count(*) from teacher where t_name like "李%";
输出
_c0
1
7、查询学过"张三"老师授课的同学的信息:
select student.* from student join score on student.s_id=score.s_id
join course on score.c_id=course.c_id join teacher on course.t_id=teacher.t_id where teacher.t_name="张三";
输出：
student.s_id    student.s_name  student.s_birth student.s_sex
1       赵雷    1990-01-01      男
2       钱电    1990-12-21      男
3       孙风    1990-05-20      男
4       李云    1990-08-06      男
5       周梅    1991-12-01      女
7       郑竹    1989-07-01      女
 
8、查询没学过"张三"老师授课的同学的信息:
 
select student.* from student left join 
(select s_id from score
join course on score.c_id=course.c_id join teacher on course.t_id=teacher.t_id where teacher.t_name="张三")tmp
on student.s_id=tmp.s_id where tmp.s_id is null;
student.s_id    student.s_name  student.s_birth student.s_sex
6       吴兰    1992-03-01      女
8       王菊    1990-01-20      女
 
9、查询学过编号为"01"并且也学过编号为"02"的课程的同学的信息:
select student.* from student 
join score s1 on student.s_id=s1.s_id join score s2 on s1.s_id=s2.s_id where s1.c_id=1 and s2.c_id=2;
student.s_id    student.s_name  student.s_birth student.s_sex
1       赵雷    1990-01-01      男
2       钱电    1990-12-21      男
3       孙风    1990-05-20      男
4       李云    1990-08-06      男
5       周梅    1991-12-01      女
 
10、查询学过编号为"01"但是没有学过编号为"02"的课程的同学的信息:
select student.* from student
join (select s_id 
    from score where c_id=1)tmp1
on student.s_id=tmp1.s_id
left join (select s_id 
    from score where c_id=2)tmp2
on tmp1.s_id=tmp2.s_id 
where tmp2.s_id is null;
输出：
student.s_id    student.s_name  student.s_birth student.s_sex
6       吴兰    1992-03-01      女
 
11、查询没有学全所有课程的同学的信息:
–先查询出课程的总数量
select count(*) from course;
输出：
_c0
3
–再查询所需结果
select student.* from student 
join (select s_id 
    from score group by s_id having count(*)<3)tmp
on student.s_id=tmp.s_id; 
输出：
student.s_id    student.s_name  student.s_birth student.s_sex
5       周梅    1991-12-01      女
6       吴兰    1992-03-01      女
7       郑竹    1989-07-01      女
 
12、查询至少有一门课与学号为"01"的同学所学相同的同学的信息:
select student.* from student 
join score tmp1 
on student.s_id=tmp1.s_id
join (select c_id 
    from score where s_id=1)tmp2 on tmp1.c_id=tmp2.c_id 
group by student.s_id,student.s_name,student.s_birth,student.s_sex;
输出:
s_id     s_name    s_birth     s_sex
1       赵雷    1990-01-01      男
2       钱电    1990-12-21      男
3       孙风    1990-05-20      男
4       李云    1990-08-06      男
5       周梅    1991-12-01      女
6       吴兰    1992-03-01      女
7       郑竹    1989-07-01      女
 
13、查询和"01"号的同学学习的课程完全相同的其他同学的信息:
先查询编号01同学学习的课程
hive (zuoye)> select * from score where s_id=1;
OK
score.s_id      score.c_id      score.s_score
1       1       80
1       2       90
1       3       99
 
select student.* from student
join (select s_id 
    from score group by s_id having count(*)=3)tmp
on student.s_id=tmp.s_id;
输出
student.s_id    student.s_name  student.s_birth student.s_sex
1       赵雷    1990-01-01      男
2       钱电    1990-12-21      男
3       孙风    1990-05-20      男
4       李云    1990-08-06      男
 
14、查询没学过"张三"老师讲授的任一门课程的学生姓名:
select student.s_name from student 
left join (select s_name,sid from 
        (select s_name,course.t_id,student.s_id sid from student 
        join score on student.s_id=score.s_id
        join course on score.c_id=course.c_id)tmp1
    join teacher on tmp1.t_id=teacher.t_id and t_name="张三")tmp2
on student.s_name=tmp2.s_name where tmp2.sid is null;
输出：
student.s_name
吴兰
王菊
 
15、查询两门及其以上不及格课程的同学的学号，姓名及其平均成绩:
select student.s_id,student.s_name,avg_s_score from student 
join (select s_id 
    from score where s_score<60 group by s_id having count(*)>=2)tmp1
on student.s_id=tmp1.s_id
left join (select s_id,avg(s_score) avg_s_score 
    from score group by s_id)tmp2
on tmp1.s_id=tmp2.s_id;
输出：
student.s_id    student.s_name  avg_s_score
4       李云    33.333333333333336
6       吴兰    32.5
 
16、检索"01"课程分数小于60，按分数降序排列的学生信息:
select student.*,s_score from student 
join score on student.s_id=score.s_id and c_id=1 where s_score<60 order by s_score desc;
输出
student.s_id    student.s_name  student.s_birth student.s_sex   s_score
4       李云    1990-08-06      男      50
6       吴兰    1992-03-01      女      31
 
17、按平均成绩从高到低显示所有学生的所有课程的成绩以及平均成绩:
select tmp1.s_score chinese,tmp2.s_score math,tmp3.s_score english,avg(score.s_score) avg_s_score from score 
left join (select s_id,s_score from score where c_id=1)tmp1 on score.s_id=tmp1.s_id
left join (select s_id,s_score from score where c_id=2)tmp2 on score.s_id=tmp2.s_id
left join (select s_id,s_score from score where c_id=3)tmp3 on score.s_id=tmp3.s_id
group by score.s_id,tmp1.s_score,tmp2.s_score,tmp3.s_score order by avg_s_score desc;
输出：
NULL    89      98      93.5
80      90      99      89.66666666666667
76      87      NULL    81.5
80      80      80      80.0
70      60      80      70.0
50      30      20      33.333333333333336
31      NULL    34      32.5

18.查询各科成绩最高分、最低分和平均分：以如下形式显示：课程ID，课程name，最高分，最低分，平均分，及格率，中等率，优良率，优秀率:
select course.c_id,c_name,max_grade,min_grade,avg_grade,pass_rade,m_rade,good_rade,perfact_rade from course
join(selectc_id,max(s_score) max_grade,min(s_score) min_grade,avg(s_score) avg_grade,
    round((sum(case when s_score>=60 then 1 else 0 end))/count(c_id),2) pass_rade,
    round((sum(case when s_score>=60 and s_score<70 then 1 else 0 end))/count(c_id),2) m_rade,
    round((sum(case when s_score>=70 and s_score<90 then 1 else 0 end))/count(c_id),2) good_rade,
    round((sum(case when s_score>=90 and s_score<100 then 1 else 0 end))/count(c_id),2) perfact_rade
    from score group by score.c_id)tmp 
on course.c_id=tmp.c_id;
输出：
course.c_id     c_name  max_grade       min_grade       avg_grade       pass_rade       m_rade  good_rade      perfact_rade
1       语文    80      31      64.5    0.67    0.0     0.67    0.0
2       数学    90      30      72.66666666666667       0.83    0.17    0.5     0.17
3       英语    99      20      68.5    0.67    0.0     0.33    0.33
 
19、按各科成绩进行排序，并显示排名:
select tmp1.*,row_number() over(order by tmp1.s_score desc) from score tmp1 where tmp1.c_id=1
union all 
select tmp2.*,row_number() over(order by tmp2.s_score desc) from score tmp2 where tmp2.c_id=2
union all
select tmp3.*,row_number() over(order by tmp3.s_score desc) from score tmp3 where tmp3.c_id=3;
输出
_u1.s_id        _u1.c_id        _u1.s_score     _u1.row_number_window_0
3       1       80      1
1       1       80      2
5       1       76      3
2       1       70      4
4       1       50      5
6       1       31      6
1       2       90      1
7       2       89      2
5       2       87      3
3       2       80      4
2       2       60      5
4       2       30      6
1       3       99      1
7       3       98      2
3       3       80      3
2       3       80      4
6       3       34      5
4       3       20      6
 
20、查询学生的总成绩并进行排名:
select student.s_name,num,sum_grade from student 
join (select s_id,sum(s_score) sum_grade,row_number()over(order by sum(s_score) desc) num from score group by s_id)tmp
on student.s_id=tmp.s_id;
输出：
student.s_name  num     sum_grade
赵雷    1       269
孙风    2       240
钱电    3       210
郑竹    4       187
周梅    5       163
李云    6       100
吴兰    7       65
 
21、查询不同老师所教不同课程平均分从高到低显示:
select tmp.avg_grade,t_name,c_name from teacher 
join course 
on teacher.t_id=course.t_id
join (select c_id,avg(s_score) avg_grade 
    from score group by c_id order by avg_grade)tmp
on tmp.c_id=course.c_id;
输出
tmp.avg_grade   t_name  c_name
64.5    李四    语文
72.66666666666667       张三    数学
68.5    王五    英语
 
22、查询所有课程的成绩第2名到第3名的学生信息及该课程成绩:
select tmp1.* from 
(select student.*,s_score,row_number()over(order by s_score desc) num from score 
    join student on score.s_id=student.s_id where c_id=1)tmp1
where num>=2 and num<=3
union all
select tmp2.* from 
(select student.*,s_score,row_number()over(order by s_score desc) num from score 
    join student on score.s_id=student.s_id where c_id=2)tmp2
where num>=2 and num<=3
union all
select tmp3.* from 
(select student.*,s_score,row_number()over(order by s_score desc) num from score 
    join student on score.s_id=student.s_id where c_id=3)tmp3
where num>=2 and num<=3;
输出
_u1.s_id        _u1.s_name      _u1.s_birth     _u1.s_sex       _u1.s_score     _u1.num
1       赵雷    1990-01-01      男      80      2
5       周梅    1991-12-01      女      76      3
7       郑竹    1989-07-01      女      89      2
5       周梅    1991-12-01      女      87      3
7       郑竹    1989-07-01      女      98      2
3       孙风    1990-05-20      男      80      3
  
23、统计各科成绩各分数段人数：课程编号,课程名称,[100-85],(85-70],(70-60],[0-60)及所占百分比
select score.c_id,course.c_name,round(sum(case when s_score>=85 and s_score<=100 then 1 else 0 end)/count(score.c_id),2) from score 
join course on score.c_id=course.c_id group by score.c_id,course.c_name
union all 
select score.c_id,course.c_name,round(sum(case when s_score>=70 and s_score<85 then 1 else 0 end)/count(score.c_id),2) from score
join course on score.c_id=course.c_id group by score.c_id,course.c_name
union all 
select score.c_id,course.c_name,round(sum(case when s_score>=60 and s_score<70 then 1 else 0 end)/count(score.c_id),2) from score  
join course on score.c_id=course.c_id group by score.c_id,course.c_name
union all
select score.c_id,course.c_name,round(sum(case when s_score>=0 and s_score<60 then 1 else 0 end)/count(score.c_id),2) from score  
join course on score.c_id=course.c_id group by score.c_id,course.c_name;
输出：
_u1.c_id        _u1.c_name      _u1._c2
1       语文    0.0
2       数学    0.5
3       英语    0.33
1       语文    0.67
2       数学    0.17
3       英语    0.33
1       语文    0.0
2       数学    0.17
3       英语    0.0
1       语文    0.33
2       数学    0.17
3       英语    0.33
 
24、查询学生平均成绩及其名次:
select round(avg(s_score),2),row_number()over(order by round(avg(s_score),2) desc) num,s_id from score group by s_id;
输出：
_c0     num     s_id
93.5    1       7
89.67   2       1
81.5    3       5
80.0    4       3
70.0    5       2
33.33   6       4
32.5    7       6
 
25、查询各科成绩前三名的记录(三个语句) 
分别查询c_id=1的前三名
select c_id,s_id,s_score,row_number()over(order by s_score desc) from score where c_id=1 limit 3;
输出
c_id    s_id    s_score row_number_window_0
1       3       80      1
1       1       80      2
1       5       76      3
 
查询c_id=2的前三名
select c_id,s_id,s_score,row_number()over(order by s_score desc) from score where c_id=2 limit 3;
输出
c_id    s_id    s_score row_number_window_0
2       1       90      1
2       7       89      2
2       5       87      3
 
查询c_id=2的前三名
select c_id,s_id,s_score,row_number()over(order by s_score desc) from score where c_id=3 limit 3;
输出
c_id    s_id    s_score row_number_window_0
3       1       99      1
3       7       98      2
3       3       80      3
 
26、查询每门课程被选修的学生数:
select c_id,count(*) from score group by c_id;
c_id    _c1
1       6
2       6
3       6
 
27、查询出只有两门课程的全部学生的学号和姓名:
select * from 
(select student.s_name,student.s_id,count(*) num from student 
    join score on student.s_id=score.s_id 
    group by student.s_id,student.s_name)tmp
where tmp.num=2;
tmp.s_name      tmp.s_id        tmp.num
周梅    5       2
吴兰    6       2
郑竹    7       2
 
28、查询男生、女生人数:
select s_sex,count(*) from student group by s_sex;
输出
s_sex   _c1
女      4
男      4
 
29、查询名字中含有"风"字的学生信息:
select student.s_name from student where s_name like "%风%";
输出
student.s_name
孙风
 
30、查询同名同性学生名单，并统计同名人数:
select s1.s_sex,s1.s_name,count(*) same_sex_name from 
student s1,student s2 where s1.s_sex=s2.s_sex and s1.s_name=s2.s_name
group by s1.s_sex,s1.s_name having same_sex_name>=2;
输出：
s1.s_sex        s1.s_name       same_sex_name
  
31、查询1990年出生的学生名单:
select * from student where year(s_birth)=1990;
输出
student.s_id    student.s_name  student.s_birth student.s_sex
1       赵雷    1990-01-01      男
2       钱电    1990-12-21      男
3       孙风    1990-05-20      男
4       李云    1990-08-06      男
8       王菊    1990-01-20      女
 
32、查询每门课程的平均成绩，结果按平均成绩降序排列，平均成绩相同时，按课程编号升序排列:
select c_id,avg(s_score) avg_grade from score group by c_id order by avg_grade desc,c_id asc;  
输出
c_id    avg_grade
2       72.66666666666667
3       68.5
1       64.5
 
33、查询平均成绩大于等于85的所有学生的学号、姓名和平均成绩:
select student.s_id,s_name,avg(s_score) avg_grade from student 
join score 
on student.s_id=score.s_id
group by student.s_id,s_name having avg_grade>=85;
输出
student.s_id    s_name  avg_grade
1       赵雷    89.66666666666667
7       郑竹    93.5
 
34、查询课程名称为"数学"，且分数低于60的学生姓名和分数:
select s_name,s_score from student 
join score on student.s_id=score.s_id
join course on score.c_id=course.c_id where c_name="数学" and s_score<60;
输出
s_name  s_score
李云    30
 
35、查询所有学生的课程及分数情况:
select student.s_id,c_id,s_score from student left join score where student.s_id=score.s_id; 
输出：
student.s_id    c_id    s_score
1       1       80
1       2       90
1       3       99
2       1       70
2       2       60
2       3       80
3       1       80
3       2       80
3       3       80
4       1       50
4       2       30
4       3       20
5       1       76
5       2       87
6       1       31
6       3       34
7       2       89
7       3       98
 
36、查询任何一门课程成绩在70分以上的学生姓名、课程名称和分数:
select s_name,c_name,s_score from 
(select s_id,c_id,s_score from score where s_score>70)tmp
left join student
on student.s_id=tmp.s_id
left join course on course.c_id=tmp.c_id;
输出：
s_name  c_name  s_score
赵雷    语文    80
赵雷    数学    90
赵雷    英语    99
钱电    英语    80
孙风    语文    80
孙风    数学    80
孙风    英语    80
周梅    语文    76
周梅    数学    87
郑竹    数学    89
郑竹    英语    98
 
37、查询课程不及格的学生:
select s_id from score where s_score<60 group by s_id having count(*)>0;
输出
s_id
4
6
 
38、查询课程编号为01且课程成绩在80分以上的学生的学号和姓名:
select student.s_id,s_score from student join score on student.s_id=score.s_id
where score.c_id=1 and s_score>80;
输出
student.s_id    s_score
 
39、求每门课程的学生人数:
select count(*) from score group by c_id;
输出
_c0
6
6
6
  
40、查询选修"张三"老师所授课程的学生中，成绩最高的学生信息及其成绩:
select student.*,score.s_score from teacher 
join course on  teacher.t_id=course.t_id
join score on course.c_id=score.c_id
join student on student.s_id=score.s_id
where t_name="张三" order by s_score desc limit 1;
输出
student.s_id    student.s_name  student.s_birth student.s_sex   score.s_score
1       赵雷    1990-01-01      男      90

41、查询不同课程成绩相同的学生的学生编号、课程编号、学生成绩:
select distinct tmp1.s_id,tmp1.c_id,tmp1.s_score from score tmp1,score tmp2 
where tmp1.c_id!=tmp2.c_id and tmp1.s_score=tmp2.s_score;
输出: 
tmp1.s_id       tmp1.c_id       tmp1.s_score
1       1       80
2       3       80
3       1       80
3       2       80
3       3       80

42、查询每门课程成绩最好的前三名:
select tmp1.* from
(select *,row_number()over(order by s_score desc) num from score where c_id=1)tmp1 
where tmp1.num<=3
union all   
select tmp2.* from
(select *,row_number()over(order by s_score desc) num from score where c_id=2)tmp2 
where tmp2.num<=3
union all
select tmp3.* from
(select *,row_number()over(order by s_score desc) num from score where c_id=3)tmp3 
where tmp3.num<=3;
输出：
_u1.s_id        _u1.c_id        _u1.s_score     _u1.num
3       1       80      1
1       1       80      2
5       1       76      3
1       2       90      1
7       2       89      2
5       2       87      3
1       3       99      1
7       3       98      2
3       3       80      3
 
43、统计每门课程的学生选修人数（超过5人的课程才统计）:
– 要求输出课程号和选修人数，查询结果按人数降序排列，若人数相同，按课程号升序排列
select c_id,count(c_id) num from score group by c_id having num>5 order by num desc,c_id asc;
输出：c_id    num
1       6
2       6
3       6
 
44、检索至少选修两门课程的学生学号:
select s_id,count(*) num from score group by s_id having num>=2;
输出：
s_id    num
1       3
2       3
3       3
4       3
5       2
6       2
7       2
 
45、查询选修了全部课程的学生信息:
select * from 
(select score.s_id from score group by s_id having count(*)=3)tmp1 
left join student on tmp1.s_id=student.s_id;
输出：
tmp1.s_id       student.s_id    student.s_name  student.s_birth student.s_sex
1       1       赵雷    1990-01-01      男
2       2       钱电    1990-12-21      男
3       3       孙风    1990-05-20      男
4       4       李云    1990-08-06      男
 
46、查询各学生的年龄(周岁):
– 按照出生日期来算，当前月日 < 出生年月的月日则，年龄减一
select s_id,year(current_date)-year(s_birth)+case when(month(current_date)-month(s_birth)>=0 and day(current_date)-day(s_birth)>=0)
then 0 else -1 end from student; 
输出
s_id    _c1
1       29
2       28
3       28
4       28
5       27
6       26
7       29
8       28
 
47、查询本周过生日的学生:
select s_id,s_birth from student where weekofyear(current_date)=weekofyear(s_birth);
输出
s_id    s_birth
 
48、查询下周过生日的学生:
select s_id,s_birth from student where weekofyear(current_date)+1=weekofyear(s_birth);
输出
s_id    s_birth
 
49、查询本月过生日的学生:
select s_id,s_birth from student where month(current_date)=month(s_birth);
输出
s_id    s_birth
1       1990-01-01
8       1990-01-20
 
50、查询12月份过生日的学生:
select s_id,s_birth from student where month(s_birth)="12";
输出:
s_id    s_birth
2       1990-12-21
5       1991-12-01
