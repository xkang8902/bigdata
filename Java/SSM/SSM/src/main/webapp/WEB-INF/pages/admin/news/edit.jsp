<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/pages/common/taglibs.jsp"%>

<e:override name="js_css_extend">
    <script>window.PROJECT_CONTEXT = "${ctx}/";</script>
    <script type="text/javascript" charset="utf-8" src="${ctx }/static/js/ueditor/ueditor.config.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx }/static/js/ueditor/ueditor.all.min.js"> </script>   
    <script type="text/javascript" charset="utf-8" src="${ctx }/static/js/ueditor/lang/zh-cn/zh-cn.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx }/static/js/cnaac/admin/news/news.js"></script>   
</e:override>

<e:override name="page_main_content">
    <input type="hidden" id="operationType" value="edit" />
    <input type="hidden" id="uuid" value="${news.uuid }" />
	<div class="po_title"><div>编辑资讯</div><span>系统首页 / 安全资讯管理 / 编辑资讯</span></div>
		<div class="contain" style="float:left;margin-top:0">	    
		    <div id="_news_div" style="display:block;">
		      	<div class="radiodiv" style="margin-top:10px;">		      	
			      	<table style="width:800px;text-align:left;">
			      	  <tr>
			      	      <td style="width:50px;"><label>标题<small style="color: red;">*</small></label></td>
			      	      <td style="width:750px;"><input required rightMessage_span id="title" name="title" type="text" class="text-style" value="${news.title }" maxLen="50"></td>
			      	  </tr>	
			      	   <tr>
			      	      <td><label>作者</label></td>
			      	      <td><input id="author" name="author" type="text" class="text-style" value="${news.author }" rightMessage_span maxLen="100"></td>
			      	  </tr>	
			      	   <tr>
			      	      <td><label>出处</label></td>
			      	      <td><input id="provenance" name="provenance" type="text" class="text-style" value="${news.provenance }" rightMessage_span maxLen="500"></td>
			      	  </tr>				      	  	      	
			      	</table>		      	
				</div>
			    <div style="clear: both;text-align:left;padding-left:4px;">
					内容<small style="color: red;">*</small><br/><input type="hidden" ueditor />
					<textarea id="contents" name="contents" type="text/plain" style="width:800px;height:350px;text-align:left;">${content }</textarea>					
			        
			    </div>
			    <div style="text-align:left;margin-top:10px;">
			        <table>
			      	  <tr>
			      	      <td valign="top"><label>摘要<small style="color: red;">*</small></label></td>			      	     
			      	  </tr>		
			      	  <tr>
			      	       <td><textarea required id="summary" name="summary" rows="5" style="width:800px;border:1px solid #ccc;" maxLen="1000">${news.summary }</textarea></td>
			      	  </tr>		      	   	      	
			      	</table>	
			    	<span>
			      		<input id="SaveSubmit" name="SaveSubmit" type="button" class="btn-save" value="保存" />
			      		<a class="backlink" href="${ctx }/news">返回</a>
			      	</span>
			    </div>
		   	 </div>
	   
  	</div>
  	<script>
  	initUeditor();
  	</script>
</e:override>

<jsp:include page="/WEB-INF/pages/common/base.jsp" flush="true">
	<jsp:param value="addNews" name="action"/>
</jsp:include>