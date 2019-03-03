/*
 * Created by Roy Oct 14, 2014 2:28:26 PM.                          
 * Copyright (c) 2000-2015 AnXunBen. All rights reserved. 
 */
package com.isprint.cnaac.server.filter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;

import com.isprint.cnaac.server.constants.GlobalConstants;
import com.isprint.cnaac.server.exception.CnaacException;
import com.isprint.cnaac.server.exception.CnaacJsonException;

 
public class SessionIntecepter implements HandlerInterceptor  {
        
    private Logger logger = LoggerFactory.getLogger(SessionIntecepter.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws CnaacException, CnaacJsonException, ServletException, IOException {     
        request.setAttribute("startTime", System.currentTimeMillis()); 
        boolean result=false;
        
        String path = request.getContextPath();
        String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
        
        boolean ajax = "XMLHttpRequest".equals( request.getHeader("X-Requested-With") );
        String ajaxFlag = null == request.getParameter("ajax") ?  "false": request.getParameter("ajax") ;
        boolean isAjax = ajax || ajaxFlag.equalsIgnoreCase("true");
        
        HttpSession session = request.getSession();
        result = null == session.getAttribute(GlobalConstants.SESSION_USER_NAME) ? false : true ;
        if (!result){
            if (isAjax){
            	request.getRequestDispatcher("/error/403").forward(request, response);
            }else{                	
            	response.sendRedirect(basePath + "login");                 
            }
            return false;
        }
        
        //check csrf token
        if (request.getMethod().equalsIgnoreCase( WebContentGenerator.METHOD_POST) ) {
           result = validToken(request);
           if (!result){
        	   logger.error("csrf token attack, reject request");
        	   if (isAjax){
               	       request.getRequestDispatcher("/error/403").forward(request, response);
               }else{                	
                       response.sendRedirect(basePath + "error/500");                      
               }
        	   return false;
           }
        } 
        return result;
    }
    
    protected boolean validToken(HttpServletRequest request) { 
    	
    	String requestCsrfToken = request.getHeader(GlobalConstants.CSRF_TOKEN_HEADER);
    	if ( StringUtils.isBlank(requestCsrfToken) ){
    		requestCsrfToken = request.getParameter("csrfToken");
    	}
    	String sessionCsrfToken = (String)request.getSession().getAttribute(GlobalConstants.SESSION_CSRF_TOKEN);
    	
    	if ( requestCsrfToken == null || sessionCsrfToken == null || !requestCsrfToken.equalsIgnoreCase(sessionCsrfToken) ){
    		return false;
    	}
    	return true;   
    }

    @Override
    public void afterCompletion(HttpServletRequest arg0,  HttpServletResponse arg1, Object arg2, Exception arg3)
            throws Exception {
        
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
            Object arg2, ModelAndView arg3) throws Exception {
            
        long startTime = (Long)request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
       // logger.info("************:" + request.getRequestURI() + "\n execute time: "+ (endTime-startTime) );
        
    }
}