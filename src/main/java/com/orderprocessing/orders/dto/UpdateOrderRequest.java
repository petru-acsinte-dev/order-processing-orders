package com.orderprocessing.orders.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

public class UpdateOrderRequest {

	@Schema(description = "List of product external ids and quantities to add or update")
	@Valid
	private List<OrderLineRequest> upsertProducts;

	@Schema(description = "List of product external ids for products to remove")
	private List<UUID> removedProducts;

	public List<OrderLineRequest> getUpsertProducts() {
		return upsertProducts;
	}

	public void setUpsertProducts(List<OrderLineRequest> updatedProducts) {
		this.upsertProducts = updatedProducts;
	}

	public List<UUID> getRemovedProducts() {
		return removedProducts;
	}

	public void setRemovedProducts(List<UUID> removedProducts) {
		this.removedProducts = removedProducts;
	}

}
