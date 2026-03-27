package com.orderprocessing.orders.controllers;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.orderprocessing.common.exceptions.ApiError;
import com.orderprocessing.common.exceptions.ApiException;

@RestControllerAdvice(basePackages = "com.orderprocessing.orders")
public class OrderControllerAdvice {

	private static Logger log = LoggerFactory.getLogger(OrderControllerAdvice.class);

	private final MessageSource messageSource;

	public OrderControllerAdvice(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiError> handleApiException(ApiException ex, Locale locale) {
		log.error("Exception caught: {}",  //$NON-NLS-1$
				(null == ex.getMessage() ? ex.getClass().getCanonicalName() : ex.getMessage()),
				ex);
		final String message = messageSource.getMessage(
                ex.getMessageKey(),
                ex.getArgs(),
                locale
        );

		final ApiError error = new ApiError(ex.getErrorCode(), message);

		return ResponseEntity
				.status(ex.getStatus())
				.body(error);
	}
}
