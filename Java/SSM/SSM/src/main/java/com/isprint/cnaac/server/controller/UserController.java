package com.isprint.cnaac.server.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.isprint.cnaac.server.constants.GlobalConstants;
import com.isprint.cnaac.server.domain.entity.AuditHistory;
import com.isprint.cnaac.server.domain.entity.Company;
import com.isprint.cnaac.server.domain.entity.User;
import com.isprint.cnaac.server.domain.vo.JsonResult;
import com.isprint.cnaac.server.domain.vo.PageVO;
import com.isprint.cnaac.server.exception.CnaacJsonException;
import com.isprint.cnaac.server.service.AuditHistoryService;
import com.isprint.cnaac.server.service.CompanyService;
import com.isprint.cnaac.server.service.UserService;


@Controller
@RequestMapping( value = "user" )
public class UserController extends BaseController {
	
	private final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private AuditHistoryService auditHistoryService;
	
	
	@RequestMapping(value = "")
	public ModelAndView listPage(@RequestParam(required = false, defaultValue = "") String cond,
			                     @RequestParam(required = false, defaultValue = "1") int page,
			                     @RequestParam(required = false, defaultValue = "10") int pageSize,
			                     HttpServletRequest request){	
		
		PageVO pagedNews = userService.getUsers(page, pageSize, cond);
		ModelAndView mv = new ModelAndView("/admin/user/list");
		mv.addObject("users", pagedNews);		
		mv.addObject("cond", cond);	
		return mv;
	}
	
	@RequestMapping(value = "/audit/{uuid}", method = RequestMethod.GET)
	public ModelAndView auditPage(@PathVariable(value = "uuid") String uuid, HttpServletRequest request) throws Exception{
		
		ModelAndView mv = new ModelAndView("/admin/user/audit");
		User user = userService.getUserByUUID(uuid);
		if ( user != null ){
			mv.addObject("user", user);
		    //企业用户 or AAAS				
			if  ( user.getUserType().equals(GlobalConstants.USER_INFO_TYPE_COMPANY) ){
				Company company = companyService.getCompanyByUUID(user.getCompanyUuid());				
				mv.addObject("company", company);
			}			
			//得到身份证头像
			/*String photoPath = user.getIdentityCardPhoto();
			if ( StringUtils.isNotBlank(photoPath) ){
				String imgStr = userService.getUserPhoto(user.getIdentityCardPhoto());
				mv.addObject("photo", imgStr);
			}*/
			//得到最后一次的拒绝或通过的原因.
			AuditHistory auditHistory = auditHistoryService.getLatestedCommentByUserUUID(uuid);
			mv.addObject("audit", auditHistory);
		}
		else{
			logger.error("cannot get user by uuid: " + uuid);
			throw new Exception("can not get user by uuid " + uuid);
		}
		mv.addObject("uuid", uuid);
		return mv;
	}
	
	@RequestMapping(value = "/audit/{uuid}", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult audit(@PathVariable(value = "uuid") String uuid, 
			                @RequestParam(required = true) String result,
			                @RequestParam(required = false, defaultValue = "") String comment,
			                HttpServletRequest request) throws CnaacJsonException{
		
		JsonResult jr = new JsonResult();
		try{
			if ( !result.equalsIgnoreCase(GlobalConstants.USER_STATUS_APPROVED) ){
				if ( comment.trim().equalsIgnoreCase("") ){
					throw new CnaacJsonException("reject the user, but there is no reason");
				}
			}
			String auditBy = request.getSession().getAttribute(GlobalConstants.SESSION_USER_NAME).toString();
			auditHistoryService.addAuditHistory(uuid, result, comment, auditBy);			
			jr.setErrorCode(GlobalConstants.OPERATION_SUCCEED);			
		}
		catch(Exception e){
			logger.error("audit user error uuid = " + uuid + ",message:" + e.getMessage());
			throw new CnaacJsonException("audit user failed");
		}
		return jr;
	}
	
	@RequestMapping(value = "/photo/{uuid}", method = RequestMethod.GET)
	public void getPrivatePic(@PathVariable(value = "uuid") String uuid,HttpServletRequest request,
			                  HttpServletResponse response){
		try {
		    response.setHeader("Pragma","No-cache");
		    response.setHeader("Cache-Control","no-cache");
		    response.setDateHeader("Expires", 0);
		    BufferedImage bufferedImage = userService.getUserPhotoByUUID(uuid);				     				    
		    if ( bufferedImage != null ){
		    	ImageIO.write(bufferedImage, "JPEG", response.getOutputStream());
		    }
		} catch (IOException e) {			
			logger.error("get photo error: " + e.getMessage());
		}
		 
	}

}
