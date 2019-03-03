package com.isprint.cnaac.server.utils;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

/**
* @ClassName: ApplicationUtil 
* @Description: TODO(simple description this class what to do.) 
* @author Yong Ge Tan 
* @version 1.0
 */
public class ApplicationUtil {
	
	
	private static ApplicationUtil appUtil = new ApplicationUtil();

	/**
	* @Title: getInstance 
	* @Description: TODO get ApplicationUtil single instance
	* @author Yong Ge Tan 
	* @date Aug 26, 2014 10:42:52 AM 
	* @return 
	* @return ApplicationUtil
	 */
	public static ApplicationUtil getInstance(){
		return appUtil;
	}
	
	/**
	* @Title: getProjectPath 
	* @Description: TODO get project real path(deployment of address) and note that the end with "/"
	* @author Yong Ge Tan 
	* @date Aug 7, 2014 6:05:46 PM 
	* @return 
	* @return String
	 */
	public String getProjectPathSeparator(){
		String result = ApplicationUtil.class.getResource("ApplicationUtil.class").getPath().toString();
		int index = result.indexOf("WEB-INF");
		if (index == -1) {
			index = result.indexOf("bin");
		}
		result = result.substring(0, index);
		if (result.startsWith("file")) {
			result = result.substring(6);
		}else if (result.startsWith("jar")) {
			result = result.substring(10);
		}
		if (result.startsWith("/"))
			result = result.substring(1);
		return result;
	}
	/**
	* @Title: getProjectPath 
	* @Description: TODO get project real path(deployment of address) and note that end without "/"
	* @author Yong Ge Tan 
	* @date Aug 8, 2014 10:48:23 AM 
	* @return 
	* @return String
	 */
	public String getProjectPath(){
		String result = getProjectPathSeparator();
		if (result.endsWith("/"))
			result = result.substring(0, result.length() - 1);
		return result;
	}
	
	/**
	* @Title: getApplicationName 
	* @Description: TODO get application name(WAR file name)
	* @author Yong Ge Tan 
	* @date Aug 7, 2014 5:32:46 PM 
	* @return 
	* @return String
	 */
	public String getApplicationName() {
		String result = getProjectPath();
		result = result.substring(result.lastIndexOf("/")+1);
		return result;
	}

	/**
	* @Title: getBasePath 
	* @Description: TODO get the server path and note that end without "/"
	* @author YG Tan 
	* @date Sep 2, 2014 3:22:14 PM 
	* @param request
	* @return  for example：http://domain:port/serverName
	* @return String
	 */
    public String getBasePath(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer();
        sb.append("http://");
        sb.append(request.getServerName());
        sb.append(":");
        sb.append(request.getServerPort());
        sb.append(request.getContextPath());
        return sb.toString();
    }

    /**
    * @Title: getBasePathSeparator 
    * @Description: TODO get the server path and note that end with "/"
    * @author YG Tan 
    * @date Sep 2, 2014 3:25:09 PM 
    * @param request
    * @return  for example：http://domain:port/serverName/
    * @return String
     */
    public String getBasePathSeparator(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer();
        sb.append(getBasePath(request));
        sb.append("/");
        return sb.toString();
    }
}
