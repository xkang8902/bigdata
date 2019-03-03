/*
 * Created by Roy Oct 14, 2014 2:28:26 PM.                          
 * Copyright (c) 2000-2014 AnXunBen. All rights reserved. 
 */
package com.isprint.cnaac.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.isprint.cnaac.server.constants.GlobalConstants;
import com.isprint.cnaac.server.domain.vo.JsonResult;
import com.isprint.cnaac.server.exception.CnaacException;
import com.isprint.cnaac.server.exception.CnaacJsonException;

public class BaseController {
    
    private Logger logger = LoggerFactory.getLogger(BaseController.class);
    
    @Autowired  
    private  HttpServletRequest request;
    
    /**
    * @Title: getUserName 
    * @Description: TODO(simple description this method what to do.) 
    * @author yongget 
    * @date Dec 4, 2014 1:20:42 PM 
    * @return 
    * @return String
     */
    public String getUserName(){
        String userName = (String) request.getSession().getAttribute(GlobalConstants.SESSION_USER_NAME);
        return userName;
    }
    /**
    * @Title: getRequest 
    * @Description: TODO(simple description this method what to do.) 
    * @author yongget 
    * @date Dec 4, 2014 4:25:26 PM 
    * @return 
    * @return HttpServletRequest
     */
    public HttpServletRequest getRequest(){
        return this.request;
    }
    
    /**
     * handle all exception include unknown exception
     * @param request
     * @param ex
     * @return
     */
    @ExceptionHandler
    public ModelAndView exceptionHandle(HttpServletRequest request, Exception ex) {   
    	boolean ajax = "XMLHttpRequest".equals( request.getHeader("X-Requested-With") );
        String ajaxFlag = null == request.getParameter("ajax") ?  "false": request.getParameter("ajax") ;
        boolean isAjax = ajax || ajaxFlag.equalsIgnoreCase("true");
        if (isAjax){
        	return new ModelAndView("redirect:/error/500");
        }
        logger.error("500 Exception - ", ex);           
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("message", "");
        return mv;          
    }
    
    /**
     * return error page when session time out.
     * @param request
     * @param ex
     * @return
     */
    @ExceptionHandler(CnaacException.class )
    public ModelAndView sessionExceptionHandle(HttpServletRequest request, Exception ex) {   
        logger.error("500 Exception - ", ex);             
        ModelAndView mv = new ModelAndView("error");       
        return mv;          
    }
    
    /**
     * return json format string for ajax request if error occurs.
     * @param request
     * @param ex
     * @return
     */
    @ExceptionHandler(CnaacJsonException.class )
    @ResponseBody
    public JsonResult sessionJsonExceptionHandle(HttpServletRequest request, Exception ex) {   
        JsonResult jr =new JsonResult();
        jr.setErrorCode(GlobalConstants.OPERATION_FAILED);
        jr.setMessage(ex.getMessage());
        return jr;
    }
    
    
}
