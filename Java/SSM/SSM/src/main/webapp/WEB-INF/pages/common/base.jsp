<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>    
<%@ include file="taglibs.jsp"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8" />
	<title>MyBatis 后台管理系统 www.yihaomen.com出品</title>
	<link rel="shortcut icon" href="${ctx }/static/images/cnaac.ico" type="image/x-icon" />
	<script type="text/javascript" src="${ctx }/static/js/jquery/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="${ctx }/static/js/validator/validator.js"></script> 
    <script type="text/javascript" src="${ctx }/static/js/validator/utils.js"></script> 
    <script type="text/javascript" src="${ctx }/static/js/dialog/dialog.js"></script>
    <script type="text/javascript" src="${ctx }/static/js/cnaac/common.js"></script> 
    <link href="${ctx }/static/css/style.css" rel="stylesheet" type="text/css" />
    <link href="${ctx }/static/css/dialog/dialog.css" rel="stylesheet" type="text/css" />
    <link href="${ctx }/static/css/common.css" rel="stylesheet" type="text/css" />
    
    <e:block name="js_css_extend"></e:block>
</head>
<body style="min-width:900px;">

	<div id="head">
		<div id="head_left">
			www.yihaomen.com MyBatis Sample
		</div>
		<div id="head_right">
			欢迎 <b><c:out value="${sessionScope.session_user_email}" /></b>
			| <a href="${ctx }/logout">注销</a> &nbsp;&nbsp;
		</div>
	</div>
    <div id="__hint_div"  onclick="javascript:$(this).hide();"></div>
	<table border="0" cellspacing="0" cellpadding="0" style="min-width:800px;width:100%;">
		<tr>
			<td valign="top" class="left_menu">
			    <e:block name="page_left_memu">
					<div>
						<span>用户管理</span>
					</div>
					<ul>
						<li <c:if test="${param.action=='user'}"> class="active" </c:if>><a href="${ctx }/user">用户列表</a></li>						
					</ul>					
					<div>
						<span>资讯管理</span>
					</div>
					<ul>
						<li <c:if test="${param.action=='addNews'}"> class="active" </c:if>><a href="${ctx }/news/create">添加资讯</a></li>
						<li <c:if test="${param.action=='listNews'}"> class="active" </c:if>><a href="${ctx }/news">资讯列表</a></li>
					</ul>
					<div>
						<span>商品管理</span>
					</div>
					<ul>
						<li <c:if test="${param.action=='listProduct'}"> class="active" </c:if>><a href="${ctx }/products">商品管理</a></li>						
					</ul>
			    </e:block>
			</td>
			
			<td height="100%" valign="top" class="right_main">
			      <input type="hidden" id="csrf_token" value="${sessionScope.session_csrf_token}" />
			      <e:block name="page_main_content">
			      </e:block>
			</td>
		</tr>
	</table>
</body>
</html>