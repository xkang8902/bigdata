package com.isprint.cnaac.server.dao;

import com.isprint.cnaac.server.domain.entity.Company;

public interface CompanyMapper {
    int deleteByPrimaryKey(String uuid);

    int insert(Company record);

    int insertSelective(Company record);

    Company selectByPrimaryKey(String uuid);

    int updateByPrimaryKeySelective(Company record);

    int updateByPrimaryKey(Company record);
}