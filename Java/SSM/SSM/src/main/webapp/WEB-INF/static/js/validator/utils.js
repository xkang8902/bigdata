/**
 * get cookies
 * @param cookieName
 * @returns
 */
function getCookie(cookieName){
	var value = document.cookie;
	var start = value.indexOf(" " + cookieName + "=");
	
	if(start == -1){
    	start = value.indexOf(cookieName + "=");
    }

    if(start == -1){
    	value = null;          
    } else {
    	start = value.indexOf("=", start) + 1;
    	var end = value.indexOf(";", start);
    	if(end == -1){
        	end = value.length;
        }
        value = unescape(value.substring(start, end));
    }
    return value;
}

/**
 * the function that dynamic load javascript 
 * */
function loadScript(urls, callback) {
    var dtd = $.Deferred();
	var addScript = function() {
		if (urls.length >= 1) {
			var s = document.createElement("script");
			s.type = "text/javascript";
			s.onload = s.onerror = s.onreadystatechange = function () {
				if (!s.readyState || s.readyState === "loaded" || s.readyState === "complete"){
					s.onload = s.onerror = s.onreadystatechange = null;
					$("head")[0].removeChild(s);
					urls.shift();
					addScript();
				}
			};
			s.src = urls[0];
			$("head")[0].appendChild(s);
		}
		else{
			if (callback){
				callback();
			}
			dtd.resolve();
		}
        return dtd;
	};
	
	return addScript();
}	

/**
 * the function to return i18n message
 */
function getJSLocale(key,params){
	var result = ""; 
	var paramsObj = {};	
	if(params) paramsObj = params;
	
	if(typeof(key) != 'undefined' && typeof(JSLocale) != 'undefined'){
		//根据key取得对应的资源内容，如果没有找到则返回key值
		if(JSLocale[key] != undefined){
			result = JSLocale[key];
		}else{
			result = key;
		}		
		// 替换对应参数为value的值
		var regExp = new RegExp(); //替换资源中参数的正则
		for(var k in paramsObj){
			regExp = eval("/{{:" + k + "}}/g");
			result = result.replace(regExp,paramsObj[k]);
		}		
		// 如果没有找到对应的资源则返回 ""
		if(/{{:[a-zA-Z]+}}/.test(result)){
			result = result.replace(/{{:[a-zA-Z]+}}/g, "");
		}
	}
	return result;
}
