$(document).ready(function(){
	var validator = new Validator();   
    validator.bindingEvent("logindiv");
    var lang = getCookie("lang") || "zh_CN";
    var scriptUrl = getContextPath() + "/static/js/validator/locale_" + lang + ".js";   		   
    loadScript([scriptUrl]);
    
    $("input[name='loginbtn']").die().live("click", function(){
    	submitLogin();
    }); 
    
    // 登陆界面就一个按钮，所以绑到body 上，否则帮到password框上面.
    $("body").keydown(function(event){  
	  if(event.keyCode==13){  
		  submitLogin();
	  }  
    }); 
    
});

function submitLogin(){
	var validator = new Validator(function(divId){	       
	   });   
	   var dtdList = [];    	   
	   var ok = validator.validateDiv("logindiv");
    dtdList.push(ok);    
    $.when.apply(null, dtdList).fail(function(){        
    }).done(function(){
    	login();
    });
}

function login(){
	$("#SaveSubmit").attr("disabled",true);	
	var user = {};
    user.email  = $("#email").val();
    user.password = $("#password").val();  
    url = getContextPath() + "/login";
    var request = {url:url, data:user,type:"POST",async:true,dataType:"json"};
    
    return ajaxCall( request ).done(function(data){
    	if (data.errorCode == "200" ){
    		window.location = getContextPath() + "/user";
    	}else if (data.errorCode == "1000" ){
    		$("#email").siblings("[validationError]").remove();	
    		$("#email").parent().append("<p validationError class='mark'>用户不存在</p>");
    	}else if (data.errorCode == "1100" || data.errorCode == '10020'){
    		$("#password").siblings("[validationError]").remove();	
    		$("#password").parent().append("<p validationError class='mark'>密码错误</p>");
    	}else if (data.errorCode == "10105"){
    		$("#password").siblings("[validationError]").remove();	
    		$("#password").parent().append("<p validationError class='mark'>账户被锁定</p>");
    	}
    }).fail(function(){
    	
    });
}