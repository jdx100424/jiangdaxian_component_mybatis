package com.jiangdaxian.mybatis.pagelimit;

import java.io.Serializable;

import org.apache.ibatis.session.RowBounds;

public class PageLimitBounds extends RowBounds implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static int NO_PAGE = 1;
	public final static int NO_ROW_LIMIT = 10;
	/** 页号 */
	protected int page = NO_PAGE;
	/** 分页大小 */
	protected int limit = NO_ROW_LIMIT;

	/** 结果集是否包含TotalCount */
	protected boolean containsTotalCount;

	public PageLimitBounds() {
		this(NO_PAGE,NO_ROW_LIMIT,false);
	}

	public PageLimitBounds(RowBounds rowBounds) {
		if (rowBounds instanceof PageLimitBounds) {
			PageLimitBounds pageBounds = (PageLimitBounds) rowBounds;
			this.page = pageBounds.page;
			this.limit = pageBounds.limit;
			this.containsTotalCount = pageBounds.containsTotalCount;
		} else {
			this.page = (rowBounds.getOffset() / rowBounds.getLimit()) + 1;
			this.limit = rowBounds.getLimit();
		}

	}
	
	/**
	 * Query TOP N, default containsTotalCount = false
	 * 
	 * @param limit
	 */
	public PageLimitBounds(int limit) {
		this(NO_PAGE,limit,false);
	}

	public PageLimitBounds(int page, int limit) {
		this(page,limit,false);
	}
	public PageLimitBounds(int page, int limit,boolean containsTotalCount) {
		this.limit = limit;
		this.page = page;
		this.containsTotalCount = containsTotalCount;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public boolean isContainsTotalCount() {
		return containsTotalCount;
	}

	public void setContainsTotalCount(boolean containsTotalCount) {
		this.containsTotalCount = containsTotalCount;
	}
}
