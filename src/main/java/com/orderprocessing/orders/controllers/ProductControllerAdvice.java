package com.orderprocessing.orders.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.orderprocessing.common.exceptions.ApiError;
import com.orderprocessing.common.exceptions.ApiErrors;
import com.orderprocessing.orders.exceptions.ProductNotFoundException;

@RestControllerAdvice
public class ProductControllerAdvice {

	@ExceptionHandler(ProductNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleProductNotFound(ProductNotFoundException ex) {
		return new ApiError(ApiErrors.PRODUCT_NOT_FOUND, ex.getMessage());
	}

}
