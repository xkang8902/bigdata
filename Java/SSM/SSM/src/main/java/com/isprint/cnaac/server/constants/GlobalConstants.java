/**   
 * Copyright (c) 2004-2015 i-Sprint Technologies, Inc.
 * address: 
 * All rights reserved. 
 * 
 * This software is the confidential and proprietary information of 
 * i-Sprint Technologies, Inc. ("Confidential Information").  You shall not 
 * disclose such Confidential Information and shall use it only in 
 * accordance with the terms of the license agreement you entered into 
 * with i-Sprint. 
 *
 * @Title: GlobalConstants.java 
 * @author yongget 
 * @Package com.isprint.cnaac.protal.constants 
 * @Description: TODO(simple description this file what to do.) 
 * @date Mar 12, 2015 10:52:30 AM 
 * @version V1.0   
*/
package com.isprint.cnaac.server.constants;

import com.isprint.cnaac.server.utils.ConfigManager;

/** 
 * @ClassName: GlobalConstants 
 * @Description: TODO(simple description this class what to do.) 
 * @author yongget 
 * @version 1.0 
 */
public class GlobalConstants {
	
	public static final String FOLDER_PUBLIC = ConfigManager.getConfigValue("PUBLIC_FOLDER");
	
	public static final String FOLDER_PRIVATE_PREFIX = "";

	public static final String FOLDER_PRIVATE = ConfigManager.getConfigValue("PRIVATE_FOLDER").replace("\\", "/");
	
	public static final String CSRF_TOKEN_HEADER = "CNAACCSRFTOKEN";
	
	public static final String SESSION_CSRF_TOKEN = "session_csrf_token";
			
    public static final String SESSION_USER_NAME = "session_user_name";
    
    public static final String SESSION_USER_EMAIL = "session_user_email";
    
    public static final String SESSION_TIMEOUT = "700";
    
    public static final String VALIDATION_ERROR_CODE = "600";
    
    public static final String OPERATION_SUCCEED = "200";
    
    public static final String OPERATION_FAILED = "500";
    
    public static final String USER_DOES_NOT_EXIST = "1000";
    
    public static final String USER_LOGIN_FAILED = "1100";
    
    //用户类型
    public static final String USER_INFO_TYPE_PERSON = "1";
    
    public static final String USER_INFO_TYPE_COMPANY = "2";
    
    public static final String USER_INFO_TYPE_AAAS = "3";
    
    public static final String USER_INFO_TYPE_CHANNEL = "4";
    
    //用户状态，审核通过or 拒绝
    public static final String USER_STATUS_APPROVED = "3";
    
    public static final String USER_STATUS_REJECTED = "4"; 
}
