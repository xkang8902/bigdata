<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/pages/common/taglibs.jsp"%>

<e:override name="js_css_extend">  
    <script type="text/javascript" charset="utf-8" src="${ctx }/static/js/cnaac/admin/user/list.js"></script>   
</e:override>

<e:override name="page_main_content">
	<div class="po_title">
		<div>用户列表</div>
		<span>系统首页 / 用户管理 / 用户列表</span>
	</div>
	<div class="table_list">
	    <div class="table_search">
			<input type="text" class="search" name="searchCond" id="upCond" value="${cond }"/>
			<input name="搜 索" type="button" class="mybutton" value="搜 索"  onclick="javascript:searchUser('upCond');"/>
		</div>
		<input type="hidden" id="maxPage" value="${users.totalPage }" />
		<input type="hidden" id="_current_page" value="${users.currentPage }" />
		<div class="table_page">
			页数 <a name="previousPageBtn" title="上一页" href="javascript:void(0);"><</a><input type="text"  title="输入页码,按回车键快速跳转" name="pageGoto" id="current_page" value="${users.currentPage }" style="text-align:center;" /><a name="nextPageBtn" title="下一页" href="javascript:void(0);">></a> 共 ${users.totalPage }页 | 查看 
			<select size="1" name="example_length" aria-controls="example" id="page_size" name="page_size_1">
				<option value="10" <c:if test="${users.pageSize==10}"> selected </c:if> >10</option>
				<option value="25" <c:if test="${users.pageSize==25}"> selected </c:if> >25</option>
				<option value="50" <c:if test="${users.pageSize==50}"> selected </c:if> >50</option>
				<option value="100" <c:if test="${users.pageSize==100}"> selected </c:if> >100</option>
			</select>&nbsp;条记录 | 共 ${users.totalCount } 条记录
		</div>
		
	</div>

	<div class="table_list_main">
		<table width="100%" border="0" cellpadding="0" cellspacing="1" bgcolor="#dcdcdc">
			<tr>
				<td  bgcolor="#efefef" nowrap="nowrap"><span class="table_title">姓名</span></td>
				<td  bgcolor="#efefef" nowrap="nowrap"><span class="table_title">邮箱</span></td>
				<td  bgcolor="#efefef" nowrap="nowrap"><span class="table_title">营业执照/身份证号</span></td>
				<!-- <td  bgcolor="#efefef"><span class="table_title">账号</span></td> -->
				<td  bgcolor="#efefef" nowrap="nowrap"><span class="table_title">类型</span></td>	
				<td  bgcolor="#efefef" nowrap="nowrap"><span class="table_title">注册时间</span></td>						
				<td  bgcolor="#efefef" nowrap="nowrap"><span class="table_title">操作</span></td>
			</tr>
			<c:forEach items="${users.listObject }" var="item">
			<tr bgcolor="#FFFFFF">
				<td nowrap><c:out value="${item.name }" /></td>
				<td nowrap><c:out value="${item.email }" /></td>
				<td nowrap>
				     <c:choose>					   
					    <c:when test="${item.userType=='1'}">
					        <c:out value="${item.identityCard }" />
					    </c:when>
					    <c:when test="${item.userType=='4'}">
					        <c:out value="${item.identityCard }" />
					    </c:when>
					    <c:when test="${item.userType=='2'}">
					        <c:out value="${item.license }" />
					    </c:when>					   
					    <c:otherwise>
					                
					    </c:otherwise>
					</c:choose>				
				</td>				
				<td nowrap>
				    <c:choose>
					    <c:when test="${item.userType=='0'}">
					                  管理员
					    </c:when>
					    <c:when test="${item.userType=='1'}">
					                  个人用户
					    </c:when>
					    <c:when test="${item.userType=='2'}">
					                  企业用户
					    </c:when>
					    <c:when test="${item.userType=='3'}">
					      AAAS用户
					    </c:when>
					    <c:when test="${item.userType=='4'}">
					                  渠道用户
					    </c:when>
					    <c:otherwise>
					                  其他用户
					    </c:otherwise>
					</c:choose>				
				</td>
				<td nowrap><fmt:formatDate value="${item.createTime }" var="formattedDate"  type="date" pattern="yyyy-MM-dd hh:mm:ss" />
	                ${formattedDate}</td>
				<td nowrap>
				   <c:choose>
					    <c:when test="${item.userStatus=='3'}">
					      <div class="edit">
					           <a href="${ctx }/user/audit/${item.uuid}">已审核</a>
					       </div>    
					    </c:when>
					    <c:when test="${item.userStatus=='1'}">
					        &nbsp;&nbsp; 未激活
					    </c:when>						   
					    <c:otherwise>
					       <div class="edit">
					           <a href="${ctx }/user/audit/${item.uuid}">审核</a>
					       </div>
					    </c:otherwise>
					</c:choose>	
				</td>
			</tr>
			</c:forEach>
			
		</table>
	</div>
	<div class="table_list">	
	    <div class="table_search">
			<input type="text" class="search" name="searchCond" id="downCond" value="${cond }">
			<input name="搜 索"  type="button" class="mybutton" value="搜 索" onclick="javascript:searchUser('downCond');"/>
		</div>
		
		<div class="table_page">
			页数 <a name="previousPageBtn" title="上一页" href="javascript:void(0);"><</a><input type="text" id="current_page" name="pageGoto" value="${users.currentPage }"  title="输入页码,按回车键快速跳转" style="text-align:center;" /><a name="nextPageBtn" title="下一页" href="javascript:void(0);">></a> 共 ${users.totalPage } 页| 查看 
			<select size="1" name="example_length" aria-controls="example" id="page_size_2" name="page_size_2">
				<option value="10" <c:if test="${users.pageSize==10}"> selected </c:if> >10</option>
				<option value="25" <c:if test="${users.pageSize==25}"> selected </c:if> >25</option>
				<option value="50" <c:if test="${users.pageSize==50}"> selected </c:if> >50</option>
				<option value="100" <c:if test="${users.pageSize==100}"> selected </c:if> >100</option>
			</select>&nbsp;条记录 | 共 ${users.totalCount } 条记录
		</div>
		
	</div>
</e:override>

<jsp:include page="/WEB-INF/pages/common/base.jsp" flush="true">
	<jsp:param value="user" name="action"/>
</jsp:include>