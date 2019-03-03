#coding=UTF-8

import random
import time

url_paths=[
    "class/112.html",
    "class/128.html",
    "class/145.html",
    "class/130.html",
    "class/146.html",
    "class/131.html",
    "learn/821",
    "course/list"
]

ip_slices=[132,156,124,10,29,167,143,187,30,46,55,63,72,87,98,168]

http_referers=[
    "https://www.baidu.com/s?wd={query}",
    "https://www.sogou.com/web?query={query}",
    "https://cn.bing.com/search?q={query}",
    "https://www.so.com/s?q={query}"
]

search_keyword=[
    "spark sql实战",
    "hadoop 基础",
    "storm实战",
    "spark streaming实战"
]

status_code=["200","404","500"]

def sample_status_code():
    return random.sample(status_code,1)[0]

def sample_referer():
    if random.uniform(0,1)>0.2:
        return "-"
    refer_str=random.sample(http_referers,1)
    query_str=random.sample(search_keyword,1)
    return refer_str[0].format(query=query_str[0])

def sample_url():
    return random.sample(url_paths,1)[0]

def sample_ip():
    slice=random.sample(ip_slices,4)
    return ".".join([str(item) for item in slice])

def generate_log(count=10):
    time_str=time.strftime("%Y-%m-%d %H:%M:%S",time.localtime())

    f=open("/Users/Mac/testdata/streaming_access.log","w+")

    while count >=1:
        query_log="{ip}\t{local_time}\t\"GET /{url} HTTP/1.1\"\t{status_code}\t{refer}".format(url=sample_url(),ip=sample_ip(),refer=sample_referer(),status_code=sample_status_code(),local_time=time_str)
        print(query_log)
        f.write(query_log+"\n")
        count=count-1

if __name__ == '__main__':
    # 每一分钟生成一次日志信息
    while True:
        generate_log()
        time.sleep(60)
