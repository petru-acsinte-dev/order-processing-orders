package com.orderprocessing.orders.exceptions;

import com.orderprocessing.common.exceptions.ApiErrors;
import com.orderprocessing.common.exceptions.BadRequestApiException;

public class EmptyProductsListException extends BadRequestApiException {

	private static final long serialVersionUID = 1L;

	public EmptyProductsListException() {
		super(ApiErrors.EMPTY_PRODUCTS_LIST, MessageKeys.EMPTY_PRODUCTS_LIST);
	}

}
