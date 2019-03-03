<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/pages/common/taglibs.jsp"%>

<e:override name="js_css_extend">  
    <script type="text/javascript" charset="utf-8" src="${ctx }/static/js/cnaac/admin/user/user.js"></script>  
    <style>
       td .right{text-align:right;width:80px;}
       td .left{text-align:left;padding-left:20px;}
       td .left img{width:400px;height:auto;}
       td .text{width:90%;height:25px;line-height:25px;}
    </style> 
</e:override>

<e:override name="page_main_content">
<div class="po_title">
	<div>用户审核</div>
	<span>系统首页 / 用户管理 / 用户审核</span>
</div>
<input type="hidden" id="uuid" name="uuid" value="${user.uuid }" />
<div class="table_list" style="min-width:600px;width:100%;">
	<table>
	    <tr>
		    <td class="right">用户类型</td>
		    <td class="left">
		         <c:choose>
				    <c:when test="${user.userType=='0'}">
				                  管理员
				    </c:when>
				    <c:when test="${user.userType=='1'}">
				                  个人用户
				    </c:when>
				    <c:when test="${user.userType=='2'}">
				                  企业用户
				    </c:when>
				    <c:when test="${user.userType=='3'}">
				      AAAS用户
				    </c:when>
				    <c:when test="${user.userType=='4'}">
				                  渠道用户
				    </c:when>
				    <c:otherwise>
				                  其他类型
				    </c:otherwise>
				</c:choose>		
		    </td>	 
		</tr>
	    <tr>
		    <td class="right">邮箱</td>
		    <td class="left" width="500"><input type="text" class="text-style" value=<c:out value='${user.email }'/> disabled /></td>	    
		</tr>
		
		<tr >
		    <td class="right">姓名</td>
		    <td class="left"><input type="text" class="text-style" value=<c:out value="${user.name }" /> disabled /></td>	 
		</tr>
		<!-- 个人信息 -->
		<c:if test="${user.userType=='1'}">
			<tr>
			    <td class="right">身份证</td>
			    <td class="left"><input type="text" class="text-style" value=<c:out value="${user.identityCard }" /> disabled /></td>	 
			</tr>
			<tr>
			    <td class="right">身份证照片</td>
			    <td class="left">
			    
				<img src='${ctx }/user/photo/${user.uuid}'/>
						    
			    </td>
			</tr>
		</c:if>
		<!-- 渠道用户 -->
		<c:if test="${user.userType=='4'}">
			<tr>
			    <td class="right">身份证</td>
			    <td class="left"><input type="text" class="text-style" value=<c:out value="${user.identityCard }" /> disabled /></td>	 
			</tr>
			<tr>
			    <td class="right">身份证照片</td>
			    <td class="left">
			    
				<img src='${ctx }/user/photo/${user.uuid}'/>
						    
			    </td>
			</tr>
		</c:if>
		<!-- 企业信息 -->
		<c:if test="${user.userType=='2'}">
			<tr>
			    <td class="right">公司名称</td>
			    <td class="left"><input type="text" class="text-style" value=<c:out value="${company.name }" /> disabled /></td>	 
			</tr>
			<tr>
			    <td class="right">营业执照</td>
			    <td class="left"><input type="text" class="text-style" value=<c:out value="${company.license }" /> disabled /></td>	 
			</tr>
			<tr>
			    <td class="right">营业执照照片</td>
			    <td class="left">
			        <img src='${ctx }/user/photo/${user.uuid}'/>
			    </td>
			</tr>
		</c:if>
		<!-- AAAS暂无 -->
		<tr>
		    <td class="right">出品人信息</td>
		    <td class="left"><input type="text" class="text-style" value=<c:out value="${user.producer }" /> disabled /></td>
		</tr>
		<tr>
		    <td class="right">电话</td>
		    <td class="left"><input type="text" class="text-style" value=<c:out value="${user.mobilePhone }" /> disabled /></td>
		</tr>
		<tr>
		    <td class="right">QQ</td>
		    <td class="left"><input type="text" class="text-style" value=<c:out value="${user.qq }"/> disabled /></td>
		</tr>
		<tr>
		    <td class="right">地址</td>
		    <td class="left"><input type="text" class="text-style" value=<c:out value="${user.address }" /> disabled /></td>
		</tr>
		<tr>
		    <td class="right">审核</td>
		    <td class="left" id="audit_radio">		    
		        <c:choose>
				    <c:when test="${user.userStatus=='3'}">
				                 已审核通过
				    </c:when>
				    <c:otherwise>
				        <input type="radio" name="approve" value="3"  <c:if test="${user.userStatus=='3'}"> checked="checked" </c:if> />通过
			            <input type="radio" name="approve" value="4" <c:if test="${user.userStatus=='4'}"> checked="checked" </c:if> />不通过
				    </c:otherwise>
				</c:choose>		       
		    </td>
		</tr>
		<c:if test="${user.userStatus!='3'}">
		<tr>
		    <td class="right">原因</td>
		    <td class="left" id="audit_comment">
		    <textarea id="comment" name="comment" rows="8" style="width:90%;border:1px solid #ccc;">${audit.reason }</textarea>
		    </td>
		</tr>
		</c:if>	
	</table>
	<div style="text-align:left;padding-left:95px;">
	     <c:choose>
		    <c:when test="${user.userStatus=='3'}">	
		         <input name="backBtn" type="button" class="btn-save" onclick="javascript:history.go(-1);" value="返回">	        
		    </c:when>		   
		    <c:otherwise>
		         <input name="auditSubmit" type="button" class="btn-save" onclick="" value="确定">    
		         <a class="backlink" href="${ctx }/user">返回</a>   
		    </c:otherwise>
		</c:choose>
	</div>
</div>
	
</e:override>

<jsp:include page="/WEB-INF/pages/common/base.jsp" flush="true">
	<jsp:param value="user" name="action"/>
</jsp:include>