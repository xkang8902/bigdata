package com.isprint.cnaac.server.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.isprint.cnaac.server.dao.plugin.PageInfo;
import com.isprint.cnaac.server.domain.dto.UserCompanyDTO;
import com.isprint.cnaac.server.domain.entity.User;

public interface UserMapper {
    int deleteByPrimaryKey(String uuid);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(String uuid);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
    
    User selectAdminUserByEmail(String email);
    
    public List<UserCompanyDTO> selectPaginationListUsers(@Param("page") PageInfo page, @Param("cond") String cond);    
    
}