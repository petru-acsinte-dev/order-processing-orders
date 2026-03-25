package com.orderprocessing.orders.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class CreateOrderRequest {

	// product external id, quantity
	@NotEmpty
	private List<OrderLineRequest> products = new ArrayList<>();

	public List<OrderLineRequest> getProducts() {
		return products;
	}

	public void setProducts(List<OrderLineRequest> products) {
		this.products = products;
	}

}
