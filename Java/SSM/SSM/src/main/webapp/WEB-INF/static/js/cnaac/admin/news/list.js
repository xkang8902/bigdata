$(document).ready(function(){
	var validator = new Validator();   
    validator.bindingEvent("_news_div");
    var lang = getCookie("lang") || "zh_CN";
    var scriptUrl = getContextPath() + "/static/js/validator/locale_" + lang + ".js";   		   
    loadScript([scriptUrl]);
        
    $("a[name='previousPageBtn']").die().live("click", function(){
    	page("/news", $(this), -1);
    });
    
    $("a[name='nextPageBtn']").die().live("click", function(){
    	page("/news", $(this), 1);
    });
    
    $("#page_size").die().live("change", function(){    	
    	$("#page_size_2").val( $("#page_size").val() );
    	page("/news", $(this), 1, $("#page_size").val() );
    });
    
    $("#page_size_2").die().live("change", function(){
    	$("#page_size").val( $("#page_size_2").val() );
    	page("/news", $(this), 1, $("#page_size_2").val() );
    }); 
    
    $('.table_list_main table tr').hover(function() {
		$(this).addClass("tr_on").removeClass("tr_off");
	}, function() {
		$(this).addClass("tr_off").removeClass("tr_on");
	});
    
    $("input[name='pageGoto']").die().live("keypress", function(event){
    	if(event.keyCode != 13) { 
    	     $("#__hint_div").hide(); 
    	}else{
    		 page("/news", $(this), $(this).val(), $("#page_size").val() );
    	}
    });
    
    $("input[name='searchCond']").die().live("keydown", function(event){
    	if(event.keyCode == 13) { 
    		searchNews($(this).attr("id"));
    	}
    });
    
    $("#__hint_div").hide();    
});

function deleteNews(uuid, message){
	cnaac.confirm("你确定要删除吗?", function(){
		var url = getContextPath() + "/news/deletion/" + uuid;
		var headers = getHeader();
		var request = {url:url, data:{},type:"POST",async:true,dataType:"json",headers:headers};
		return ajaxCall( request ).done(function(data){
			handleAjaxCallback(data, "删除成功", getContextPath() + "/news");			
		}).fail(function(){
			cnaac.alert("删除失败");
		});
	});	
}

function searchNews(queryID){
	var keyword = $.trim( $("#" + queryID).val() );
	var pageSize = $("#page_size").val();
	var params = genSearchObj(getContextPath() + "/news", 1, pageSize,keyword);	
	mockFormSubmit(params);	
}


