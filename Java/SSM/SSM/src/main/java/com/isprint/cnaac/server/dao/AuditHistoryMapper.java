package com.isprint.cnaac.server.dao;

import com.isprint.cnaac.server.domain.entity.AuditHistory;

public interface AuditHistoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AuditHistory record);

    int insertSelective(AuditHistory record);

    AuditHistory selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AuditHistory record);

    int updateByPrimaryKey(AuditHistory record);
    
    AuditHistory getLatestedCommentByUserUUID(String uuid);
}