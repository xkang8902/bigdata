<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/pages/common/taglibs.jsp"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>后台管理系统</title>
<link rel="shortcut icon" href="${ctx }/static/images/cnaac.ico" type="image/x-icon" />
<link href="${ctx }/static/css/login.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${ctx }/static/js/jquery/jquery-1.7.2.min.js"></script> 
<script type="text/javascript" src="${ctx }/static/js/validator/validator.js"></script> 
<script type="text/javascript" src="${ctx }/static/js/validator/utils.js"></script> 
<script type="text/javascript" src="${ctx }/static/js/cnaac/common.js"></script> 
<script type="text/javascript" src="${ctx }/static/js/cnaac/login.js"></script>  
</head>
<body class="mybody">
<div class="background">
  <div class="head">
    <h3>MyBatis Sample 后台管理系统</h3>
  </div>
  <div id="logindiv" class="logindiv"><span class="tabname">用户登录</span>
    <div class="container" style="top:59px">
      <div class="left">
        <h4>账号信息</h4>
        <ul>
			<li>
				<label>邮箱信息:</label>
				<span>
				<input required email loginvalidation type="text" value="123456@qq.com" name="email" id="email" class="text-style" />
				</span>
			</li>
			<li>
				<label>密码:</label>
				<span>
				<input required loginvalidation type="password" name="password" value="123456" id="password" class="text-style" />
				</span>
			</li>
            <li>
				<label></label>
				<span>
				<input required class="login-btn" type="button" name="loginbtn"  value="登陆"/>
				</span>
			</li>
        </ul>
      </div>
     
    </div>
  </div>
</div>
<div class="bottom">
  <div class="footer" style="color:#fff;" >
    <p>Copyright © 2015 www.yihaomen.com</p>
  </div>
</div>
</body>
</html>
