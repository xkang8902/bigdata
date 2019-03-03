package com.sid.com.project
 
import com.ggstar.util.ip.IpHelper
 
object IpUtils {
  def getCity(ip:String) ={
    IpHelper.findRegionByIp(ip)
  }
 
  def main(args: Array[String]): Unit = {
    println(getCity("117.35.88.11"))
  }
}
