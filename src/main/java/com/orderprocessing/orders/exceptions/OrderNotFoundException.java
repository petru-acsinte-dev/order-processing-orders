package com.orderprocessing.orders.exceptions;

import java.util.UUID;

import com.orderprocessing.common.exceptions.ApiErrors;
import com.orderprocessing.common.exceptions.NotFoundApiException;

public class OrderNotFoundException extends NotFoundApiException {

	private static final long serialVersionUID = 1L;

	public OrderNotFoundException(UUID externalId) {
		super(ApiErrors.ORDER_NOT_FOUND, MessageKeys.ORDER_NOT_FOUND, externalId);
	}

}
