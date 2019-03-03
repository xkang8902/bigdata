package com.sparkproject.dao.factory;

import com.sparkproject.dao.IAdBlacklistDAO;
import com.sparkproject.dao.IAdClickTrendDAO;
import com.sparkproject.dao.IAdProvinceTop3DAO;
import com.sparkproject.dao.IAdStatDAO;
import com.sparkproject.dao.IAdUserClickCountDAO;
import com.sparkproject.dao.IAreaTop3ProductDAO;
import com.sparkproject.dao.IPageSplitConvertRateDAO;
import com.sparkproject.dao.ISessionAggrStatDAO;
import com.sparkproject.dao.ISessionDetailDAO;
import com.sparkproject.dao.ISessionRandomExtractDAO;
import com.sparkproject.dao.ITaskDAO;
import com.sparkproject.dao.ITop10CategoryDAO;
import com.sparkproject.dao.ITop10SessionDAO;
import com.sparkproject.dao.impl.AdBlacklistDAOImpl;
import com.sparkproject.dao.impl.AdClickTrendDAOImpl;
import com.sparkproject.dao.impl.AdProvinceTop3DAOImpl;
import com.sparkproject.dao.impl.AdStatDAOImpl;
import com.sparkproject.dao.impl.AdUserClickCountDAOImpl;
import com.sparkproject.dao.impl.AreaTop3ProductDAOImpl;
import com.sparkproject.dao.impl.PageSplitConvertRateDAOImpl;
import com.sparkproject.dao.impl.SessionAggrStatDAOImpl;
import com.sparkproject.dao.impl.SessionDetailDAOImpl;
import com.sparkproject.dao.impl.SessionRandomExtractDAOImpl;
import com.sparkproject.dao.impl.TaskDAOImpl;
import com.sparkproject.dao.impl.Top10CategoryDAOImpl;
import com.sparkproject.dao.impl.Top10SessionDAOImpl;

/**
 * DAO工厂类
 * @author Administrator
 *
 */
public class DAOFactory {


	public static ITaskDAO getTaskDAO() {
		return new TaskDAOImpl();
	}

	public static ISessionAggrStatDAO getSessionAggrStatDAO() {
		return new SessionAggrStatDAOImpl();
	}
	
	public static ISessionRandomExtractDAO getSessionRandomExtractDAO() {
		return new SessionRandomExtractDAOImpl();
	}
	
	public static ISessionDetailDAO getSessionDetailDAO() {
		return new SessionDetailDAOImpl();
	}
	
	public static ITop10CategoryDAO getTop10CategoryDAO() {
		return new Top10CategoryDAOImpl();
	}
	
	public static ITop10SessionDAO getTop10SessionDAO() {
		return new Top10SessionDAOImpl();
	}
	
	public static IPageSplitConvertRateDAO getPageSplitConvertRateDAO() {
		return new PageSplitConvertRateDAOImpl();
	}
	
	public static IAreaTop3ProductDAO getAreaTop3ProductDAO() {
		return new AreaTop3ProductDAOImpl();
	}
	
	public static IAdUserClickCountDAO getAdUserClickCountDAO() {
		return new AdUserClickCountDAOImpl();
	}
	
	public static IAdBlacklistDAO getAdBlacklistDAO() {
		return new AdBlacklistDAOImpl();
	}
	
	public static IAdStatDAO getAdStatDAO() {
		return new AdStatDAOImpl();
	}
	
	public static IAdProvinceTop3DAO getAdProvinceTop3DAO() {
		return new AdProvinceTop3DAOImpl();
	}
	
	public static IAdClickTrendDAO getAdClickTrendDAO() {
		return new AdClickTrendDAOImpl();
	}
	
}
