$(document).ready(function(){
    
    $("a[name='previousPageBtn']").die().live("click", function(){
    	page("/user", $(this), -1);
    });
    
    $("a[name='nextPageBtn']").die().live("click", function(){
    	page("/user", $(this), 1);
    });
    
    $("#page_size").die().live("change", function(){    	
    	$("#page_size_2").val( $("#page_size").val() );
    	page("/user", $(this), 1, $("#page_size").val() );
    });
    
    $("#page_size_2").die().live("change", function(){
    	$("#page_size").val( $("#page_size_2").val() );
    	page("/user", $(this), 1, $("#page_size_2").val() );
    }); 
    
    $("input[name='searchCond']").die().live("keydown", function(event){
    	if(event.keyCode == 13) { 
    		searchUser($(this).attr("id"));
    	}
    });
    
    $("input[name='pageGoto']").die().live("keypress", function(event){
    	if(event.keyCode != 13) { 
    	     $("#__hint_div").hide(); 
    	}else{
    		 page("/user", $(this), $(this).val(), $("#page_size").val() );
    	}
    });
    
    $("#__hint_div").hide();
    
    $('.table_list_main table tr').hover(function() {
		$(this).addClass("tr_on").removeClass("tr_off");
	}, function() {
		$(this).addClass("tr_off").removeClass("tr_on");
	});
});

function searchUser(queryID){
	var keyword = $.trim( $("#" + queryID).val() );
	var pageSize = $("#page_size").val();	
	var params = genSearchObj(getContextPath() + "/user", 1, pageSize,keyword);	
	mockFormSubmit(params);	
}
