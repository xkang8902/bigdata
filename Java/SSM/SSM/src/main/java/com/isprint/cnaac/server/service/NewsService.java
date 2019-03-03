package com.isprint.cnaac.server.service;

import java.util.List;

import com.isprint.cnaac.server.dao.plugin.PageInfo;
import com.isprint.cnaac.server.domain.entity.News;
import com.isprint.cnaac.server.domain.vo.PageVO;

public interface NewsService {
	
	public int createNews(String user, News news);
	
	public PageVO getNews(int page, int pageSize, String title);
	
	public void deleteNewsByUUID(String uuid);
	
    public News getSingleNews(String uuid);
    
    public void updateNewsByUUID(String user, String uuid, News news);

}
