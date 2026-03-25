package com.orderprocessing.orders.exceptions;

import java.util.UUID;

import com.orderprocessing.common.exceptions.ApiErrors;
import com.orderprocessing.common.exceptions.BadRequestApiException;

public class UnknownOrderStatusException extends BadRequestApiException {

	private static final long serialVersionUID = 1L;

	public UnknownOrderStatusException(UUID externalId, String orderStatus) {
		super(ApiErrors.ORDER_STATUS_DOES_NOT_ALLOW_OP, MessageKeys.CANNOT_MODIFY_ORDER, externalId, orderStatus);
	}

}
