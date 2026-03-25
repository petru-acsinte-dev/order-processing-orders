package com.orderprocessing.orders.exceptions;

import com.orderprocessing.common.exceptions.ApiErrors;
import com.orderprocessing.common.exceptions.BadRequestApiException;

public class NonMatchingCurrencyException extends BadRequestApiException {

	private static final long serialVersionUID = 1L;

	public NonMatchingCurrencyException(String expected, String actual) {
		super(ApiErrors.INCOMPATIBLE_CURRENCIES, MessageKeys.INCOMPATIBLE_CURRENCIES, expected, actual);
	}

}
