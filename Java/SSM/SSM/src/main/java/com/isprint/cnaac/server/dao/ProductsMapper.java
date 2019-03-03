package com.isprint.cnaac.server.dao;

import java.util.List;

import com.isprint.cnaac.server.domain.entity.Products;

public interface ProductsMapper {
    int deleteByPrimaryKey(String productid);

    int insert(Products record);

    int insertSelective(Products record);

    Products selectByPrimaryKey(String productid);

    int updateByPrimaryKeySelective(Products record);

    int updateByPrimaryKey(Products record);
    
    List<Products> selectAllProducts();
}