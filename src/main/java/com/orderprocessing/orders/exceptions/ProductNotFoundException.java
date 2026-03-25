package com.orderprocessing.orders.exceptions;

import com.orderprocessing.common.exceptions.ApiErrors;
import com.orderprocessing.common.exceptions.NotFoundApiException;

public class ProductNotFoundException extends NotFoundApiException {

	private static final long serialVersionUID = 1L;

	public ProductNotFoundException() {
		super(ApiErrors.PRODUCT_NOT_FOUND, MessageKeys.PRODUCT_NOT_FOUND);
	}

}
