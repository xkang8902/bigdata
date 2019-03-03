package com.isprint.cnaac.server.service.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.isprint.cnaac.server.constants.GlobalConstants;
import com.isprint.cnaac.server.dao.AuditHistoryMapper;
import com.isprint.cnaac.server.domain.entity.AuditHistory;
import com.isprint.cnaac.server.domain.entity.User;
import com.isprint.cnaac.server.service.AuditHistoryService;
import com.isprint.cnaac.server.service.UserService;


@Service( value = "auditHistoryService")
public class AuditHistoryServieImpl implements AuditHistoryService {
	
	private final Logger logger = LoggerFactory.getLogger(AuditHistoryServieImpl.class);
	
	@Autowired
	private AuditHistoryMapper auditHistoryMapper;
	
	@Autowired
	private UserService userService;

	@Override
	public synchronized void addAuditHistory(String uuid, String result, String reason, String auditBy) throws Exception{
		logger.info("begin to audit:" + uuid + " result: " + result + " reson: " + reason);
		User user = userService.getUserByUUID(uuid);
		if ( null == user ){
			throw new Exception("can not find user by uuid: " + uuid);			
		}
		
		String status = user.getUserStatus();
		if ( status.equalsIgnoreCase(GlobalConstants.USER_STATUS_APPROVED)  ){
			throw new Exception("user has been audited, user uuid is: " + uuid);	
		}
		
		
		AuditHistory auditHistory = new AuditHistory();
		auditHistory.setUserUuid( uuid );
		auditHistory.setAuditResult( result );
		auditHistory.setReason( reason );
		auditHistory.setAuditBy(auditBy);
		auditHistory.setAuditTime( new Date() );		
		auditHistoryMapper.insertSelective(auditHistory);
		userService.updateUserStatusByUUID(uuid, result);		
		
	}

	@Override
	public AuditHistory getLatestedCommentByUserUUID(String uuid) {
		return auditHistoryMapper.getLatestedCommentByUserUUID(uuid);		
	}

}
