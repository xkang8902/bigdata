<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/pages/common/taglibs.jsp"%>

<e:override name="js_css_extend">  
    <script type="text/javascript" charset="utf-8" src="${ctx }/static/js/cnaac/admin/news/list.js"></script>   
</e:override>

<e:override name="page_main_content">
	<div class="po_title">
		<div>资讯列表</div>
		<span>系统首页 / 资讯管理 / 资讯列表</span>
	</div>
	<div class="table_list">
	    <div class="table_search">
			<input type="text" class="search" name="searchCond" id="upCond" value="${title }"/>
			<input name="搜 索" type="button"  class="mybutton" value="搜 索"  onclick="javascript:searchNews('upCond');"/>
		</div>
		<input type="hidden" id="maxPage" value="${news.totalPage }" />
		<input type="hidden" id="_current_page" value="${news.currentPage }" />
		<div class="table_page">
			页数 <a name="previousPageBtn" title="上一页" href="javascript:void(0);"><</a><input title="输入页码,按回车键快速跳转" type="text" name="pageGoto" id="current_page" value="${news.currentPage }" style="text-align:center;" /><a name="nextPageBtn" title="下一页" href="javascript:void(0);">></a> 共 ${news.totalPage } 页 | 查看 
			<select size="1" name="example_length" aria-controls="example" id="page_size" name="page_size_1">
				<option value="10" <c:if test="${news.pageSize==10}"> selected </c:if> >10</option>
				<option value="25" <c:if test="${news.pageSize==25}"> selected </c:if> >25</option>
				<option value="50" <c:if test="${news.pageSize==50}"> selected </c:if> >50</option>
				<option value="100" <c:if test="${news.pageSize==100}"> selected </c:if> >100</option>
			</select>&nbsp;条记录 | 共 ${news.totalCount } 条记录
		</div>
		
	</div>

	<div class="table_list_main">
		<table width="100%" border="0" cellpadding="0" cellspacing="1" bgcolor="#dcdcdc">
			<tr>
				<td bgcolor="#efefef" style="min-width:300px;" nowrap="nowrap"><span class="table_title">标题</span></td>
				<td width="150" bgcolor="#efefef" nowrap="nowrap"><span class="table_title">作者</span></td>
				<td width="200" bgcolor="#efefef" nowrap="nowrap"><span class="table_title">发布日期</span></td>
				<td width="100" bgcolor="#efefef" nowrap="nowrap"><span class="table_title">点击次数</span></td>				
				<td width="200" bgcolor="#efefef" nowrap="nowrap"><span class="table_title">操作</span></td>
			</tr>
			<c:forEach items="${news.listObject }" var="item">
			<tr bgcolor="#FFFFFF">
				<td nowrap><c:out value="${item.title }" /></td>
				<td nowrap><c:out value="${item.author }" /></td>
				<td nowrap>				
					<fmt:formatDate value="${item.createTime }" var="formattedDate"  type="date" pattern="yyyy-MM-dd hh:mm:ss" />
	                ${formattedDate}
				</td>
				<td nowrap>${item.clickCount }</td>
				<td nowrap><div class="edit">
						<a href="${ctx }/news/edit/${item.uuid}">编辑</a>
					</div>
					<div class="del">
						<a href="javascript:void(0);" onclick="javascript:deleteNews('${item.uuid}', '${item.title }')">删除</a>
					</div>
			    </td>
			</tr>
			</c:forEach>
		</table>
	</div>
	<div class="table_list">	
	    <div class="table_search">
			<input type="text" class="search" name="searchCond" id="downCond" value="${title }">
			<input name="搜 索"  type="button"  class="mybutton" value="搜 索" onclick="javascript:searchNews('downCond');"/>
		</div>
		
		<div class="table_page">
			页数 <a name="previousPageBtn" title="上一页" href="javascript:void(0);"><</a><input type="text" alt="输入页码,按回车键快速跳转" title="输入页码,按回车键快速跳转" name="pageGoto" id="current_page" value="${news.currentPage }"  style="text-align:center;" /><a name="nextPageBtn" title="下一页" href="javascript:void(0);">></a> 共 ${news.totalPage } 页| 查看 
			<select size="1" name="example_length" aria-controls="example" id="page_size_2" name="page_size_2">
				<option value="10" <c:if test="${news.pageSize==10}"> selected </c:if> >10</option>
				<option value="25" <c:if test="${news.pageSize==25}"> selected </c:if> >25</option>
				<option value="50" <c:if test="${news.pageSize==50}"> selected </c:if> >50</option>
				<option value="100" <c:if test="${news.pageSize==100}"> selected </c:if> >100</option>
			</select>&nbsp;条记录 | 共 ${news.totalCount } 条记录
		</div>
		
	</div>
</e:override>

<jsp:include page="/WEB-INF/pages/common/base.jsp" flush="true">
	<jsp:param value="listNews" name="action"/>
</jsp:include>