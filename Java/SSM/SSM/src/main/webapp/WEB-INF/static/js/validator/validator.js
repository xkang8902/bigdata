function Validator(errorHandle){
	this.errorHandle = errorHandle;
	this.elements = ["input","select","textarea"];
}

Validator.prototype = {
		contructor : Validator,
		rules : {},
		addRules : function(ruleName, validFunction, errorFunction){
			this.rules[ruleName] = {
					validFunction : validFunction,
					errorFunction : errorFunction
			};
		},
		
		eventFunction : function(item){
			var self = this;
			$(item).die().live("blur", function(){
				self.validateItem(item);	
			});
		},
		
		bindingEvent : function(divId){
			var self = this;
			
			self.elements.forEach(function(element){
				$("#" + divId).find(element).each(function(i, item){
					self.eventFunction(item);		
				});
			});
			
		},
		
		validateDiv : function(divId){
			var dtdList = [],
				self = this;
			
				self.elements.forEach(function(element){
					$("#" + divId).find(element).each(function(i, item){
						dtdList.push( self.validateItem(item) );
					});					
				});
			
				self.elements.forEach(function(element){
					if ($("#" + divId).find(element).length == 0){
						var deferred = $.Deferred();
						deferred.resolve();
						dtdList.push(deferred);
					}				
				});
			
			return $.when.apply(null, dtdList).fail(function(){
				if(self.errorHandle) {
					self.errorHandle.call(null, divId);					
				}
			});
			
		},
		
		validateItem : function(d){
			var self = this;
			var ruleList = [];
			for(var r in this.rules){	
				if( $(d).is("[" + r + "]") ){
					ruleList.push(r);
				}
			}
			var dtd = $.Deferred();
			var checked = function(){	
				if ( ruleList.length >= 1 ){
					var ok = self.validate(d, ruleList[0]);
					ok.done(function(){
						if(ruleList.length >= 2){
							self.validate(d, ruleList[1]);
						}
						ruleList.shift();
						checked();
					}).fail(function(){
						dtd.reject();
					});
				} else {
					dtd.resolve();
				} ;
				//dtd.resolve();
				//return dtd;
			};
			checked();
			return dtd;
			
		},
		validate : function(d, rule){
			var value = $(d).val(),
		    	attributeValue = $(d).attr(rule),
	            f = this.rules[rule].validFunction,
	            self = this;	        
	    
			var ok = f.call(null, d, value, attributeValue);
			
			return ok.fail(function(item){
				if($(item).siblings("[validationError]").length != 0 ){
			    	$(item).siblings("[validationError]").remove();					    	
			    }
				if(self.rules[rule].errorFunction){
					self.rules[rule].errorFunction(d, rule);
				}else {
					self.showErrorMessage(d, rule);					
				}
			}).done(function(item){
				if($(item).siblings("[validationError]").length != 0 ){
			    	$(item).siblings("[validationError]").remove();					    	
			    }
			});
		},
		
		validationByRegx : function(d, value, regx){
			var dtd = $.Deferred(),
			ok = regx.test(value);
		
			if(ok || $.trim(value) === ""){
				dtd.resolve(d);
			} else {
				dtd.reject(d);
			}
			return dtd.promise();
		},
		
		/*默认的出错处理方法*/
		showErrorMessage : function(item,rule){
			
			var msgInfo = getJSLocale( $(item).attr("msgid") );
			var ruleInfo = getJSLocale( "msg_" + rule );
			
			var attributeValue = $(item).attr(rule);
			if  (attributeValue){				
				msgInfo = getJSLocale( $(item).attr("msgid"), {length:attributeValue} );
				ruleInfo = getJSLocale( "msg_" + rule, {length:attributeValue} );
			}
			
			var curHtmlTagBegin = "<div validationError style='color:#ff5714'>";
			var curHtmlTagEnd = "</div>";
			var htmlTag = "div";
			if( $(item).is("[loginvalidation]") ){ /*为登陆定制的提示*/
				if($(item).siblings().find("[msgid='msg_"+ rule +"']").length == 0){
					var message = msgInfo || ruleInfo;				
					$(item).parent().append(curHtmlTagBegin +"<p class='mark' msgid='msg_" + rule +"'>&nbsp;&nbsp;" + message + "</p>" + curHtmlTagEnd);				
				}
			}else if( $(item).is("[rightMessage_span]") ){ /*显示在元素右边的提示*/
				curHtmlTagBegin = "<span validationError style='color:#ff5714'>";
				curHtmlTagEnd = "</span>";
				htmlTag = "span";
								
				if($(item).siblings(htmlTag).find("[msgid='msg_"+ rule +"']").length == 0){
					var message = msgInfo || ruleInfo;				
					$(item).parent().append(curHtmlTagBegin +"<label msgid='msg_" + rule +"'>&nbsp;&nbsp;" + message + "</label>" + curHtmlTagEnd);				
				}
			}else{/*元素下面的提示*/
				if($(item).siblings(htmlTag).find("[msgid='msg_"+ rule +"']").length == 0){
					var message = msgInfo || ruleInfo;				
					$(item).parent().append(curHtmlTagBegin +"<label msgid='msg_" + rule +"'>&nbsp;&nbsp;" + message + "</label>" + curHtmlTagEnd);				
				}
			}
		}
};

//add default rule
Validator.prototype.addRules("required", function(d, value, attributeValue){
	var dtd = $.Deferred();
	var ok = !($.trim(value) == "" || value == null);
	if(ok){
		dtd.resolve(d);
	} else {
		dtd.reject(d);
	}
	return dtd.promise();
});


Validator.prototype.addRules("maxLen", function(d, value, attributeValue){
	var dtd = $.Deferred();
	var ok = (value.length <= attributeValue);
	if(ok){
		dtd.resolve(d);
	} else {
		dtd.reject(d, attributeValue);
	}
	return dtd.promise();
	
});

Validator.prototype.addRules("minLen", function(d, value, attributeValue){
	var dtd = $.Deferred();
	var ok = (value.length >= attributeValue);
	if(ok){
		dtd.resolve(d);
	} else {
		dtd.reject(d, attributeValue);
	}
	return dtd.promise();
	
}, function(d, rule){
	if($(d).siblings("div").find("[msgid='msg_" + rule + "']").length == 0){
		var attributeValue = $(d).attr(rule);
		var msgInfo = getJSLocale( $(d).attr("msgid"), {length:attributeValue} );
		var ruleInfo = getJSLocale( "msg_" + rule, {length:attributeValue} );
		var message = msgInfo || ruleInfo;
		$(d).parent().append("<div validationError style='color:#ff5714'><label msgid='msg_" + rule + "' errorMsgParam='"+attributeValue+"'>" + message + "</label></div>");				
	}
});

Validator.prototype.addRules("ueditor", function(d, value, attributeValue){
	var dtd = $.Deferred();
	
	var ok = !($.trim( UE.getEditor('contents').getContent() ) == "");
	if(ok){
		dtd.resolve(d);
	} else {
		dtd.reject(d, attributeValue);
	}
	return dtd.promise();
	
}, function(d, rule){
	if($("#contents").siblings("div").find("[msgid='msg_" + rule + "']").length == 0){
		var attributeValue = $(d).attr(rule);
		var msgInfo = getJSLocale( $(d).attr("msgid") );
		var ruleInfo = getJSLocale( "msg_" + rule );
		var message = msgInfo || ruleInfo;
		$("#contents").parent().append("<div validationError style='color:#ff5714'><label msgid='msg_" + rule + "' errorMsgParam='"+attributeValue+"'>" + message + "</label></div>");				
	}
});

Validator.prototype.addRules("url", function(d, value, attributeValue){
	return Validator.prototype.validationByRegx(d, value, /^(((ht|f)tp(s?))\:\/\/)[a-zA-Z0-9]+\.[a-zA-Z0-9]+[\/=\?%\-&_~`@[\]\':+!]*([^<>\"\"])*$/i);
});

Validator.prototype.addRules("email", function(d, value, attributeValue){	
	return Validator.prototype.validationByRegx(d, value, /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/);
});

Validator.prototype.addRules("english", function(d, value, attributeValue){	
	return Validator.prototype.validationByRegx(d, value, /^[a-zA-Z0-9_\-]+$/);
});



/**
中国国内常用正则表达式:
var reg = new Object();
reg.english = /^[a-zA-Z0-9_\-]+$/;
reg.chinese = /^[\u0391-\uFFE5]+$/;
reg.number = /^[-\+]?\d+(\.\d+)?$/;
reg.integer = /^[-\+]?\d+$/;
reg.float = /^[-\+]?\d+(\.\d+)?$/;
reg.date = /^(\d{4})(-|\/)(\d{1,2})\2(\d{1,2})$/;
reg.email = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
reg.url = /^(((ht|f)tp(s?))\:\/\/)[a-zA-Z0-9]+\.[a-zA-Z0-9]+[\/=\?%\-&_~`@[\]\':+!]*([^<>\"\"])*$/;
*/
