function getContextPath() {
    var pathName = window.location.pathname;
    var index = pathName.substr(1).indexOf("/");
    var result = pathName.substr(0,index+1);
    return result;
}

function ajaxCall(request){
	return $.ajax(request);
}

function removeTip(my){
	my.find(".tip_msg").remove();	
}

function addTip(my,tip){
	removeTip(my);
	var tip1='<span class="tip_msg" style="color:#ff5714;"><br />';
	var tip2='</span>';
	my.append(tip1 + tip + tip2);
}

function handleAjaxCallback(data, message, successUrl, releaseButtonID){
	if ( data.errorCode == "200" ){
		var successMessage = message || "操作成功" ;
		cnaac.alert(successMessage, function(){
			window.location = successUrl;
		});
	}else if(data.errorCode == "700"){
		cnaac.alert("会话超时，请重新登录",function(){
			window.location = getContextPath() + "/login";
		});
	}else{
		if (releaseButtonID){
			$("#" + releaseButtonID).attr("disabled",false);
		}
		cnaac.alert("操作失败");
	}
}

function getHeader(){
	var headers = {};
    headers['CNAACCSRFTOKEN'] = $("#csrf_token").val();
    return headers;
}

function genSearchObj(url,page,pageSize,keyword){
	var params = {};
	params.url = url;
	params.page = page;
	params.pageSize = pageSize;
	params.cond = keyword;
	return params;
}

function mockFormSubmit(params){
	var form = $('<form />', {action : params.url, method:"post", style:"display:none;"}).appendTo('body');	
	$.each(params, function(k, v) {
	      if ( k != "url" ){
	    	  form.append('<input type="hidden" name="' + k +'" value="' + v +'" />');
	      }
	});
	form.append('<input type="hidden" name="csrfToken" value="' + $("#csrf_token").val() + '" />' );
	form.submit();
}

function showPageError(item, maxPage){
    var obj = item.parent().find("input[name='pageGoto']").eq(0);	
	var offset = obj.offset();
	var left = offset.left,
	    top = offset.top;
	var message = "页码输入有误";
	var hintDiv = $("#__hint_div");
	hintDiv.css({"top": top+24, "left": left, "position":"absolute"}).addClass("page_hint_div");
	hintDiv.html("").append(message).show();
}

function page(){
	if (arguments.length == 3 ){
	    pagination(arguments[0], arguments[1], arguments[2]);
	}else if (arguments.length == 4 ){
		pagination(arguments[0], arguments[1], arguments[2], arguments[3]);
	}
}

function pagination(){
	var regx = /^[0-9]*[1-9][0-9]*$/;
	var page = 1;
	var pageSize = $("#page_size").val();
	var maxPage = parseInt( $("#maxPage").val() );
	
	if ( arguments.length == 3 ){		
		var currentPage = parseInt( $("#_current_page").val() );		
		page = currentPage + arguments[2];
		page = page <= 0 ? 1 : page;
		page = page >= maxPage ? maxPage : page;
	}	
	
	if ( arguments.length == 4 ){
		page = arguments[2];
		pageSize = arguments[3];
	}
	
	if ( regx.test(page) ){
		if ( page>=1 && page<=maxPage ){
			
			var searchCondiction = $.trim( $("#upCond").val() ) || $.trim( $("#downCond").val() );
			if  ( searchCondiction.length > 0 ){
				var params = genSearchObj(getContextPath() + arguments[0], page, pageSize,searchCondiction);	
				mockFormSubmit(params);	
			}else{
				var params = genSearchObj(getContextPath() + arguments[0], page, pageSize,"");	
				mockFormSubmit(params);
			}
			
		}else{
			showPageError( arguments[1], maxPage );
		}
		
	}else{		
		showPageError( arguments[1], maxPage );
	}	
}