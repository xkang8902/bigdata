<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/pages/common/taglibs.jsp"%>

<e:override name="js_css_extend">  
    <script type="text/javascript" charset="utf-8" src="${ctx }/static/js/cnaac/admin/product/list.js"></script>   
</e:override>

<e:override name="page_main_content">
	<div class="po_title">
		<div>商品列表</div>
		<span>系统首页 / 商品管理 / 商品列表</span>
	</div>
	
	<div class="table_list_main">
	<table>
	   <table width="100%" border="0" cellpadding="0" cellspacing="1" bgcolor="#dcdcdc">
			<tr>
				<td bgcolor="#efefef" style="min-width:300px;" nowrap="nowrap"><span class="table_title">商品名称</span></td>
				<td width="150" bgcolor="#efefef" nowrap="nowrap"><span class="table_title">价格</span></td>						
				<td width="200" bgcolor="#efefef" nowrap="nowrap"><span class="table_title">操作</span></td>
			</tr>
		<c:forEach items="${items }" var="item">
			<tr bgcolor="#FFFFFF">
				<td nowrap><c:out value="${item.productname }" /></td>
				<td nowrap><input id="P_${item.productid}" style="width:70px;height:25px;" type="text" value="${item.productprice }" /></td>	
				<td>
				    <div class="edit">
				    <a href="javascript:void(0);" onclick="javascript:updateProduct('${item.productid}')">保存</a>
				    </div>
				</td>			
			</tr>
			</c:forEach>
	 </table>
	</div>
	
</e:override>

<jsp:include page="/WEB-INF/pages/common/base.jsp" flush="true">
	<jsp:param value="listProduct" name="action"/>
</jsp:include>