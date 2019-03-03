package com.isprint.cnaac.server.service;

import java.util.List;

import com.isprint.cnaac.server.domain.entity.Products;

public interface ProductService {
	
	public List<Products> getAllProducts();
	
	public int updateProduct(Products product);

}
