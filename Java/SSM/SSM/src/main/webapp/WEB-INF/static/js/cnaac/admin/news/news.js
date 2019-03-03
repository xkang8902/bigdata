$(document).ready(function(){
	var validator = new Validator();   
    validator.bindingEvent("_news_div");
    var lang = getCookie("lang") || "zh_CN";
    var scriptUrl = getContextPath() + "/static/js/validator/locale_" + lang + ".js";   		   
    loadScript([scriptUrl]);
    
    $("input[name='SaveSubmit']").die().live("click", function(){
    	saveNews();
    });    
});

function submitNews(){
	
	$("#SaveSubmit").attr("disabled",true);	
	var news = {};
    news.title  = $("#title").val();
    news.author = $("#author").val();
    news.provenance = $("#provenance").val();
    news.contents = UE.getEditor('contents').getContent();
    news.summary = $("#summary").val();
    url = getContextPath() + "/news/create";
    var headers = getHeader();
    var request = {url:url, data:news,type:"POST",async:true,dataType:"json",headers:headers};
    
    return ajaxCall( request ).done(function(data){
    	if (data.errorCode == "200"){
	    	cnaac.confirm("添加资讯成功, 继续添加 ？", function(){
	    		window.location = getContextPath() + "/news/create";
	    	},
	    	function(){
	    		window.location = getContextPath() + "/news";
	    	});
    	}else if(data.errorCode == "700"){
    		cnaac.alert("会话超时，请重新登录",function(){
    			window.location = getContextPath() + "/login";
    		});
    	}else{
    		cnaac.alert("操作失败");
    		$("#SaveSubmit").attr("disabled",false);
    	}
    }).fail(function(){
    	$("#SaveSubmit").attr("disabled",false);
    });
    
}

function updateNews(){
	$("#SaveSubmit").attr("disabled",true);	
	var news = {};
	news.uuid =  $("#uuid").val();
    news.title  = $("#title").val();
    news.author = $("#author").val();
    news.provenance = $("#provenance").val();
    news.contents = UE.getEditor('contents').getContent();
    news.summary = $("#summary").val();
    url = getContextPath() + "/news/edit/" +  $("#uuid").val() ;
    var headers = getHeader();
    var request = {url:url, data:news,type:"POST",async:true,dataType:"json",headers:headers};
    
    return ajaxCall( request ).done(function(data){    	
    	handleAjaxCallback(data, "更新资讯成功", getContextPath() + "/news", "SaveSubmit");	    	
    }).fail(function(){
    	$("#SaveSubmit").attr("disabled",false);
    });
}

function saveNews(){
	var validator = new Validator(function(divId){	       
	   });   
	   var dtdList = [];    	   
	   var ok = validator.validateDiv("_news_div");
    dtdList.push(ok);    
    $.when.apply(null, dtdList).fail(function(){        
    }).done(function(){
    	if ( $.trim( $("#operationType").val() ) == "add" ){
    	    submitNews();
    	}else if ( $.trim( $("#operationType").val() ) == "edit" ){
    		updateNews();
    	}    	
    });
}

/*Ueditor 比较另类，所以单独定制的一个验证方法*/
function initUeditor(){
	window.UEDITOR_CONFIG.serverUrl = window.PROJECT_CONTEXT + "ueditor/dispatch?csrfToken=" + $("#csrf_token").val() ;
	var ue = UE.getEditor('contents', {
  		   elementPathEnabled:false,
  		   enableAutoSave:false
  		});  	
     var domUtils = UE.dom.domUtils;  	
  	 ue.addListener("blur",function(){  	   
  	        var content =UE.getEditor('contents').getContent();
  	        if ( $.trim(content) == "" ){
  	        	if($("#contents").siblings("div").find("[msgid='msg_ueditor']").length == 0){  	      		
  	      		var ruleInfo = getJSLocale( "msg_ueditor");
  	      		var message = ruleInfo;
  	      		$("#contents").parent().append("<div validationError style='color:#ff5714'><label msgid='msg_ueditor' errorMsgParam='"+"null"+"'>" + message + "</label></div>");				
  	      	}}else{
  	      	   if($("#contents").siblings("[validationError]").length != 0 ) {
  	      		  $("#contents").siblings("[validationError]").remove();					    	
		       }
  	      	}
  	  });
}