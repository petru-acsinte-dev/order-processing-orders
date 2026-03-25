package com.orderprocessing.orders.exceptions;

import com.orderprocessing.common.exceptions.ApiErrors;
import com.orderprocessing.common.exceptions.BadRequestApiException;

public class TooManyProductsInRequest extends BadRequestApiException {

	private static final long serialVersionUID = 1L;

	public TooManyProductsInRequest(int systemMax, int requestSize) {
		super(ApiErrors.REQUEST_TOO_BIG, MessageKeys.REQUEST_TOO_BIG, systemMax, requestSize);
	}

}
