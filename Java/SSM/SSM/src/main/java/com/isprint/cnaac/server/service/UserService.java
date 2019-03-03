package com.isprint.cnaac.server.service;

import java.awt.image.BufferedImage;

import com.isprint.cnaac.server.domain.entity.AuditHistory;
import com.isprint.cnaac.server.domain.entity.User;
import com.isprint.cnaac.server.domain.vo.PageVO;

public interface UserService {
	
	public User getUserByEmail(String email);
	
	public User getUserByUUID(String uuid);
	
	public PageVO getUsers(int page, int pageSize, String cond);
	
	public void updateUserStatusByUUID(String uuid, String status);
	
	public String getUserPhoto(String suffix);
	
	public BufferedImage getUserPhotoByUUID(String uuid);

}
