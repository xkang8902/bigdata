package com.isprint.cnaac.server.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.isprint.cnaac.server.dao.ProductsMapper;
import com.isprint.cnaac.server.domain.entity.Products;
import com.isprint.cnaac.server.service.ProductService;


@Service("productService")
public class ProductServiceImpl implements ProductService {
	
	@Autowired
	private ProductsMapper productsMapper;
	
	public List<Products> getAllProducts() {
		return productsMapper.selectAllProducts();
	}

	@Override
	public int updateProduct(Products product) {
		return productsMapper.updateByPrimaryKeySelective(product);
	}
	
	
}
