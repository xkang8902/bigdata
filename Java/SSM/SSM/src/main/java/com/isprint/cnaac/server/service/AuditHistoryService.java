package com.isprint.cnaac.server.service;

import com.isprint.cnaac.server.domain.entity.AuditHistory;


public interface AuditHistoryService {
	
	public void addAuditHistory(String uuid, String result, String reason, String auditBy) throws Exception;
	
	public AuditHistory getLatestedCommentByUserUUID(String uuid);

}
