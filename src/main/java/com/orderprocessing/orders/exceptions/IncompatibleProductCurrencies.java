package com.orderprocessing.orders.exceptions;

import java.util.Currency;

import com.orderprocessing.common.exceptions.ApiErrors;
import com.orderprocessing.common.exceptions.BadRequestApiException;

public class IncompatibleProductCurrencies extends BadRequestApiException {

	private static final long serialVersionUID = 1L;

	public IncompatibleProductCurrencies(Currency orderCurrency, Currency productCurrency) {
		super (	ApiErrors.INCOMPATIBLE_CURRENCIES,
				MessageKeys.INCOMPATIBLE_CURRENCIES,
				orderCurrency.getCurrencyCode(),
				productCurrency.getCurrencyCode()
		);
	}

}
