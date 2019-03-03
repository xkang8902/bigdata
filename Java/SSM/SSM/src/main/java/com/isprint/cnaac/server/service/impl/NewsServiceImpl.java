package com.isprint.cnaac.server.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.isprint.cnaac.server.dao.NewsMapper;
import com.isprint.cnaac.server.dao.plugin.PageInfo;
import com.isprint.cnaac.server.domain.entity.News;
import com.isprint.cnaac.server.domain.vo.PageVO;
import com.isprint.cnaac.server.service.NewsService;


@Service( value = "newsService")
public class NewsServiceImpl implements NewsService {
	
	private final Logger logger = LoggerFactory.getLogger(NewsServiceImpl.class);
	
	@Autowired
	private NewsMapper newsMapper;

	@Override
	public int createNews(String user, News news) {	
		logger.info("add news by " + user + ", news title is " + news.getTitle());		
		String newsUUID = UUID.randomUUID().toString().replaceAll("-", "");
		news.setUuid(newsUUID);
		Date currentDate = new Date();
		news.setPubtime( currentDate );
		news.setCreateTime( currentDate );
		news.setUpdateTime( currentDate );
		news.setClickCount( 0 );
		news.setCreateBy( user);
		news.setUpdateBy( user );		
		//看内容里面是否有图片, 如果有则得到第一张图片
		String firstImage = parseFirstImage( new String(news.getContents()) );
		news.setSummaryPic(firstImage);		
		return newsMapper.insert(news);
	}


	private String parseFirstImage(String sourceString) {
		Document doc = Jsoup.parse( sourceString );  
		Element image = doc.select("img").first();
		String firstImage = "";
		if (null != image){
			firstImage = image.attr("src");			
		}
		return firstImage;
	}

	
	@Override
	public PageVO getNews(int page, int pageSize, String title) {
		logger.info("get paged news, page = " + page + ", pageSize = " + pageSize + ", title = " + title);
		PageInfo pageInfo = new PageInfo();
		if ( page <=0 ){
			page = 1;
		}
		int currentResult = (page-1) * pageSize;
		pageInfo.setShowCount(pageSize);
		pageInfo.setCurrentResult(currentResult);
		String titleCondiction = "%";
		if ( StringUtils.isNotBlank(title) ){
			titleCondiction = "%" + title + "%";
		}
		List<News> news = newsMapper.selectPaginationListNews(pageInfo, titleCondiction);
		
		int maxPage = pageInfo.getTotalResult() % pageSize == 0 ? pageInfo.getTotalResult() / pageSize : pageInfo.getTotalResult() / pageSize + 1;
		int currentPage = page < 0 ? 1 : page;
		if (maxPage > 0 ){
		    currentPage = currentPage > maxPage ? maxPage : currentPage;
		}
		PageVO pagedNews = new PageVO();
		pagedNews.setCurrentPage(currentPage);
		pagedNews.setPageSize(pageSize);
		pagedNews.setTotalCount(pageInfo.getTotalResult());
		pagedNews.setTotalPage(maxPage);
		pagedNews.setListObject(news);
		
		logger.info("get " + news.size() + " news");
		return pagedNews;
	}


	@Override
	public void deleteNewsByUUID(String uuid) {
		logger.info("begin to delete news, uuid = " + uuid);
		newsMapper.deleteByPrimaryKey(uuid);		
	}
	
	@Override
    public News getSingleNews(String uuid){
    	return newsMapper.selectByPrimaryKey(uuid);
    }
	
	@Override
	public void updateNewsByUUID(String user, String uuid, News news){
		logger.info( user + " begin to update news: uuid = " + uuid );
		News old = newsMapper.selectByPrimaryKey(uuid);
		old.setTitle( news.getTitle() );
		old.setAuthor( news.getAuthor() );
		old.setProvenance( news.getProvenance() );
		old.setContents( news.getContents() );
		old.setSummary( news.getSummary() );
		old.setUpdateBy( user );
		old.setUpdateTime( new Date() );
		String firstImage = parseFirstImage( new String(news.getContents()) );
		old.setSummaryPic(firstImage);
		newsMapper.updateByPrimaryKeyWithBLOBs(old);		
	}
	
	
	
}
