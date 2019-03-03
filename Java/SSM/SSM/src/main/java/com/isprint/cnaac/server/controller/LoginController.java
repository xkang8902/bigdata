package com.isprint.cnaac.server.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.isprint.cnaac.server.constants.GlobalConstants;
import com.isprint.cnaac.server.domain.entity.User;
import com.isprint.cnaac.server.domain.vo.JsonResult;
import com.isprint.cnaac.server.domain.vo.UserVO;

import com.isprint.cnaac.server.service.UserService;

import com.isprint.cnaac.server.utils.RandomUtil;


@Controller
public class LoginController extends BaseController{
	
	private final Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	UserService userService;
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult login( @Valid @ModelAttribute UserVO userVO, BindingResult error, HttpServletRequest request){
		
		JsonResult jr =new JsonResult();
		if (error.hasErrors()){
            jr.setErrorCode(GlobalConstants.VALIDATION_ERROR_CODE);
        }else{   
        	logger.info("login begin, user is: "+ userVO.getEmail());
        	User user = userService.getUserByEmail(userVO.getEmail());
        	if ( null == user ){
        		jr.setErrorCode(GlobalConstants.USER_DOES_NOT_EXIST);
        		logger.info("user is not exists: " + userVO.getEmail());
        	}else{
        		
        		String retCode = "200";
        				
        		if ( retCode.equalsIgnoreCase(GlobalConstants.OPERATION_SUCCEED) ){
        			HttpSession session = request.getSession();
        			session.setAttribute(GlobalConstants.SESSION_USER_NAME, user.getUserId());
        			session.setAttribute(GlobalConstants.SESSION_USER_EMAIL, userVO.getEmail().toLowerCase());
        			session.setAttribute(GlobalConstants.SESSION_CSRF_TOKEN, RandomUtil.getRandomCode(32, 4));        			
        			
        		}else{
        			logger.info("user login failed: " + userVO.getEmail());
        		}
        		jr.setErrorCode( retCode );
        	}        	
        }
		return jr;		
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView loginPage(HttpServletRequest request){		
		ModelAndView mv =new ModelAndView("/login");
		return mv;
	}
	
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ModelAndView logout(HttpServletRequest request){	
		HttpSession session = request.getSession();
		session.invalidate();
		ModelAndView mv =new ModelAndView("/login");
		return mv;
	}
	

}
