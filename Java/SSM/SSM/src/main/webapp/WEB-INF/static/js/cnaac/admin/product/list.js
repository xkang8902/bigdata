function updateProduct(pid){
	var url = getContextPath() + "/products/" + pid;
	var headers = getHeader();
	var product = {};
	product.productprice = $("#P_"+pid).val();
	product.productid = pid;
	
	if(isNaN(product.productprice)){
		cnaac.alert("商品价格是无效的数字");
		return;
	}
	
	var request = {url:url, data:product,type:"POST",async:true,dataType:"json",headers:headers};
	return ajaxCall( request ).done(function(data){
		handleAjaxCallback(data, "更新成功", getContextPath() + "/products/");			
	}).fail(function(){
		cnaac.alert("更新失败");
	});
}