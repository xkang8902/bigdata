package com.isprint.cnaac.server.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.isprint.cnaac.server.dao.plugin.PageInfo;
import com.isprint.cnaac.server.domain.entity.News;

public interface NewsMapper {
	
    int deleteByPrimaryKey(String uuid);

    int insert(News record);

    int insertSelective(News record);

    News selectByPrimaryKey(String uuid);

    int updateByPrimaryKeySelective(News record);

    int updateByPrimaryKeyWithBLOBs(News record);

    int updateByPrimaryKey(News record);
    
    public List<News> selectPaginationListNews(@Param("page") PageInfo page,@Param("title") String title);
}