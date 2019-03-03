package com.isprint.cnaac.server.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.isprint.cnaac.server.constants.GlobalConstants;
import com.isprint.cnaac.server.domain.entity.Products;
import com.isprint.cnaac.server.domain.vo.JsonResult;
import com.isprint.cnaac.server.exception.CnaacJsonException;
import com.isprint.cnaac.server.service.ProductService;


@Controller
@RequestMapping("/products")
public class ProductController extends BaseController {
	
	private final Logger logger = LoggerFactory.getLogger(NewsController.class);
	
	@Autowired
	private ProductService productService;
	
	
	@RequestMapping(value="")
	public ModelAndView getProducts(){
		
		List<Products> products = productService.getAllProducts();
		ModelAndView mv = new ModelAndView("/admin/products/list");
		mv.addObject("items", products);
		return mv;
		
	}
	
	@RequestMapping(value="/{uuid}")
	@ResponseBody
	public JsonResult updateProduct(Products products) throws Exception{
		JsonResult jr = new JsonResult();
		try{
			productService.updateProduct(products);
	    	jr.setErrorCode(GlobalConstants.OPERATION_SUCCEED); 
		}catch(Exception e){
			logger.error("update product by uuid = " + products.getProductid() + "error: " + e.getMessage());
			throw new Exception("update product failed");
		}
    	
		return jr;
		
	}

}
