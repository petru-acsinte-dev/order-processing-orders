package com.orderprocessing.orders.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

@Component
@ConfigurationProperties(prefix = "orders.demo.orders")
@Validated
public class OrderProps {

	@Positive
	private int pageSize;

	@Positive
	private int maxPageSize;

	private String defaultSortAttribute;

	@Positive
	private int queryBatchSize;

	@Positive
	private int queryMaxSize;

	/**
	 * @return The page size used for listing products, orders, order lines
	 */
	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return Maximum customizable limit for a page size.
	 * Cannot exceed {@link Constants.PAGE_SIZE_HARD_LIMIT}
	 */
	public int getMaxPageSize() {
		return maxPageSize;
	}

	public void setMaxPageSize(int maxPageSize) {
		this.maxPageSize = maxPageSize;
	}

	/**
	 * @return Default attribute to sort by
	 */
	public String getDefaultSortAttribute() {
		return defaultSortAttribute;
	}

	public void setDefaultSortAttribute(String defaultSortAttribute) {
		this.defaultSortAttribute = defaultSortAttribute;
	}

	public int getQueryBatchSize() {
		return queryBatchSize;
	}

	public void setQueryBatchSize(int queryBatchSize) {
		this.queryBatchSize = queryBatchSize;
	}

	public int getQueryMaxSize() {
		return queryMaxSize;
	}

	public void setQueryMaxSize(int queryMaxSize) {
		this.queryMaxSize = queryMaxSize;
	}

}
