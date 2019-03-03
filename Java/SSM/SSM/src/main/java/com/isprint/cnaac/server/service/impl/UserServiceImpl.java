package com.isprint.cnaac.server.service.impl;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.isprint.cnaac.server.constants.GlobalConstants;
import com.isprint.cnaac.server.dao.UserMapper;
import com.isprint.cnaac.server.dao.plugin.PageInfo;
import com.isprint.cnaac.server.domain.dto.UserCompanyDTO;
import com.isprint.cnaac.server.domain.entity.AuditHistory;
import com.isprint.cnaac.server.domain.entity.Company;
import com.isprint.cnaac.server.domain.entity.User;
import com.isprint.cnaac.server.domain.vo.PageVO;
import com.isprint.cnaac.server.service.CompanyService;
import com.isprint.cnaac.server.service.UserService;
import com.isprint.cnaac.server.utils.ApplicationUtil;
import com.isprint.cnaac.server.utils.ConfigManager;

import com.isprint.cnaac.server.utils.ImageUtils;


@Service(value = "userService")
public class UserServiceImpl implements UserService {
	
	private final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private CompanyService companyService;
	
	
	public User getUserByEmail(String email){		
		User user = userMapper.selectAdminUserByEmail(email);
		return user;
		
	}

	@Override
	public PageVO getUsers(int page, int pageSize, String cond) {
		logger.info("get paged user current page = " + page + ", pageSize = " + pageSize + ", cond = " + cond);
		PageInfo pageInfo = new PageInfo();
		if ( page <=0 ){
			page = 1;
		}
		int currentResult = (page-1) * pageSize;
		pageInfo.setShowCount(pageSize);
		pageInfo.setCurrentResult(currentResult);
		
		String condiction = "%";
		if ( StringUtils.isNotBlank(cond) ){
			condiction = "%" + cond + "%";
		}
		
		List<UserCompanyDTO> users = userMapper.selectPaginationListUsers(pageInfo, condiction);
		
		int maxPage = pageInfo.getTotalResult() % pageSize == 0 ? pageInfo.getTotalResult() / pageSize : pageInfo.getTotalResult() / pageSize + 1;
		int currentPage = page < 0 ? 1 : page;
		if ( maxPage > 0 ){
			currentPage = currentPage > maxPage ? maxPage : currentPage;
		}
		PageVO pagedUser = new PageVO();
		pagedUser.setCurrentPage(currentPage);
		pagedUser.setPageSize(pageSize);
		pagedUser.setTotalCount(pageInfo.getTotalResult());
		pagedUser.setTotalPage(maxPage);
		pagedUser.setListObject(users);
		
		logger.info("get paged user end, found " + users.size() + " records.");
		return pagedUser;
	}

	@Override
	public User getUserByUUID(String uuid) {		
		return userMapper.selectByPrimaryKey(uuid);
	}

	@Override
	public void updateUserStatusByUUID(String uuid, String status) {
		logger.info("update user status, status = " + status + " uuid = " + uuid);
		User user = userMapper.selectByPrimaryKey(uuid);
		if (user != null) {
			user.setUserStatus(status);
			userMapper.updateByPrimaryKeySelective(user);
		}
	}

	@Override
	public String getUserPhoto(String suffix) {		
		String filePath = GlobalConstants.FOLDER_PRIVATE +  File.separator + GlobalConstants.FOLDER_PRIVATE_PREFIX + suffix;
		return ImageUtils.encodeImgageToBase64( new File(filePath) );
		
	}
	
	
	@Override
	public BufferedImage getUserPhotoByUUID(String uuid) {
		logger.info("begin to get user photo by uuid: " + uuid);
		User user = getUserByUUID(uuid);
		String photoPath = "";
		BufferedImage image = null;
		if ( null != user ){
			//个人用户以及渠道用户
			if ( user.getUserType().equals(GlobalConstants.USER_INFO_TYPE_PERSON) || user.getUserType().equals(GlobalConstants.USER_INFO_TYPE_CHANNEL) ){
				photoPath = user.getIdentityCardPhoto();
			}else{
				Company company = companyService.getCompanyByUUID(user.getCompanyUuid());
				if ( null!= company ){
					photoPath = company.getLicensePhoto();
				}
			}
			if ( StringUtils.isNotBlank(photoPath) ){
				String filePath = GlobalConstants.FOLDER_PRIVATE +  File.separator + GlobalConstants.FOLDER_PRIVATE_PREFIX + photoPath;
				logger.info("get photo path: " + filePath);
				try {
					image = ImageIO.read(new File(filePath));
				} catch (IOException e) {
					image = null;
					logger.error("get photo error: " + e.getMessage());
				}		
			}
		 }
		return image;
	}

}
