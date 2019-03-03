$(document).ready(function(){
    
    $("input[name='auditSubmit']").die().live("click", function(){
    	preAudit();
    });
    
    $("input[name='approve']").die().live("click", function(){
    	updateAuditInfo();
    });    
    
});

function preAudit(){
	var auditResult = $('input[name="approve"]:checked').val();
	if (auditResult){
		if  (auditResult == "4"){
			var comment = $.trim( $("#comment").val() );
			if (comment.length <= 0){
				addTip( $("#audit_comment"), "请输入审核不通过的原因" );
				return;
			}else if (comment.length > 1000){
				addTip( $("#audit_comment"), "最大长度1000" );
				return;
			}else{
				removeTip( $("#audit_comment") );
			}
		}
	}
	else{
		addTip($("#audit_radio"), "请选择审核结果");
		return;
	}
	audit(auditResult);
}

function audit(auditResult){
	var message = auditResult == "3" ? "审核通过" : "审核不通过";	
	cnaac.confirm( message + ", 确定要继续?", function(){
		$("#auditSubmit").attr("disabled",true);
		var auditResult = $('input[name="approve"]:checked').val();
		var comment = $.trim( $("#comment").val() );
	    url = getContextPath() + "/user/audit/" + $("#uuid").val();
	    var headers = getHeader();
	    var request = {url:url, data:{result:auditResult,comment:comment},type:"POST",async:true,dataType:"json",headers:headers};
	    
	    return ajaxCall( request ).done(function(data){	    	
	    	handleAjaxCallback(data, "操作成功", getContextPath() + "/user", "auditSubmit");	    	
	    }).fail(function(){
	    	$("#auditSubmit").attr("disabled",false);
	    });
	 });
}

function updateAuditInfo(){
	removeTip( $("#audit_radio") );
	var auditResult = $('input[name="approve"]:checked').val();
	if ( auditResult == "4" ){
		var comment = $.trim( $("#comment").val() );
		if (comment.length <= 0){
			addTip($("#audit_comment"), "请输入审核不通过的原因");			
		}else{
			removeTip( $("#audit_comment") );
		}		
	}
	else{
		removeTip( $("#audit_comment") );
	}
}
