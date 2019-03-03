package com.isprint.cnaac.server.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.isprint.cnaac.server.constants.GlobalConstants;
import com.isprint.cnaac.server.domain.entity.News;
import com.isprint.cnaac.server.domain.vo.JsonResult;
import com.isprint.cnaac.server.domain.vo.PageVO;
import com.isprint.cnaac.server.exception.CnaacJsonException;
import com.isprint.cnaac.server.service.NewsService;



@Controller
@RequestMapping( value = "news" )
public class NewsController extends BaseController {
	
	private final Logger logger = LoggerFactory.getLogger(NewsController.class);
	
	@Autowired
	private NewsService newsService;
	
	
	@RequestMapping(value = "")
	public ModelAndView listPage(@RequestParam(required = false, defaultValue = "") String cond,
			                     @RequestParam(required = false, defaultValue = "1") int page,
			                     @RequestParam(required = false, defaultValue = "10") int pageSize,
			                     HttpServletRequest request){		
		
		PageVO pagedNews = newsService.getNews(page, pageSize, cond);	
		ModelAndView mv = new ModelAndView("/admin/news/list");
		mv.addObject("news", pagedNews);		
		mv.addObject("title", cond);	
		return mv;
	}
	
	@RequestMapping(value = "create", method = RequestMethod.GET)
	public ModelAndView addPage(){
		ModelAndView mv = new ModelAndView("/admin/news/add");
		return mv;
	}
	
	
	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult create(@Valid @ModelAttribute News news, BindingResult error, HttpServletRequest request) throws CnaacJsonException{
		JsonResult jr = new JsonResult();
		try{
			if (error.hasErrors()){
	            jr.setErrorCode(GlobalConstants.VALIDATION_ERROR_CODE);
	        }else{
	        	String user = request.getSession().getAttribute(GlobalConstants.SESSION_USER_NAME).toString();
	        	int row = newsService.createNews(user, news);
	        	jr.setErrorCode(GlobalConstants.OPERATION_SUCCEED);
	        }
		}catch(Exception e){
			logger.error("create news error " + e.getMessage());
			throw new CnaacJsonException("create news failed");
		}
		return jr;
	}
	
	@RequestMapping(value = "deletion/{uuid}", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult deletion(@PathVariable(value = "uuid") String uuid, HttpServletRequest request) throws CnaacJsonException{
		JsonResult jr = new JsonResult();
		try{
	    	newsService.deleteNewsByUUID(uuid);
	    	jr.setErrorCode(GlobalConstants.OPERATION_SUCCEED); 
		}catch(Exception e){
			logger.error("delete news error uuid = " + uuid + "error: " + e.getMessage());
			throw new CnaacJsonException("delete new failed");
		}
    	
		return jr;
	}
	
	@RequestMapping(value = "edit/{uuid}", method = RequestMethod.GET)
	public ModelAndView editPage(@PathVariable(value = "uuid") String uuid){
		ModelAndView mv = new ModelAndView("/admin/news/edit");
		News news = newsService.getSingleNews(uuid);
		mv.addObject("news", news);
		mv.addObject("content", new String( news.getContents() ) );
		return mv;
	}
	
	@RequestMapping(value = "edit/{uuid}", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult update(@PathVariable(value = "uuid") String uuid,
			                   @Valid @ModelAttribute News news, BindingResult error, HttpServletRequest request) throws CnaacJsonException{
		String user = request.getSession().getAttribute(GlobalConstants.SESSION_USER_NAME).toString();    	
		JsonResult jr =new JsonResult();
		try{
			if (error.hasErrors()){
	            jr.setErrorCode(GlobalConstants.VALIDATION_ERROR_CODE);
	        }else{        	
	        	newsService.updateNewsByUUID(user, uuid, news);
	        	jr.setErrorCode(GlobalConstants.OPERATION_SUCCEED);
	        }
		}catch(Exception e){
			logger.error("edit news error " + ",error: " + e.getMessage());
			throw new CnaacJsonException("update news error");
		}
		return jr;
	}

	

}
