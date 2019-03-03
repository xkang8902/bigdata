package com.isprint.cnaac.server.domain.vo;

import java.util.List;

public class PageVO {
	
	private int currentPage;
	private int pageSize;
	private int totalCount;
	private int totalPage;	
	private List listObject;

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List getListObject() {
		return listObject;
	}

	public void setListObject(List listObject) {
		this.listObject = listObject;
	}
	
	
	
	

}
