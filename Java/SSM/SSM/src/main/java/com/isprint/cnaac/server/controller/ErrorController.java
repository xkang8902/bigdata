package com.isprint.cnaac.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.isprint.cnaac.server.constants.GlobalConstants;
import com.isprint.cnaac.server.domain.vo.JsonResult;

@Controller
@RequestMapping("/error")
public class ErrorController {
	
    @RequestMapping("/403")
    @ResponseBody
    public JsonResult sessionJsonExceptionHandle(HttpServletRequest request) {   
        JsonResult jr =new JsonResult();
        //session 过期与csrf token无效都显示这个吧.
        jr.setErrorCode(GlobalConstants.SESSION_TIMEOUT);
        jr.setMessage("");
        return jr;
    }
    
    @RequestMapping("/500")
    @ResponseBody
    public JsonResult Handle500(HttpServletRequest request) {   
        JsonResult jr =new JsonResult();
        //session 过期与csrf token无效都显示这个吧.
        jr.setErrorCode(GlobalConstants.OPERATION_FAILED);
        jr.setMessage("500 exception");
        return jr;
    }


}
