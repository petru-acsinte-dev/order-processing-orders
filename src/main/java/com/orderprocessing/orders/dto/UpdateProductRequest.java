package com.orderprocessing.orders.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

/**
 * Product DTO used for product update requests.
 */
public class UpdateProductRequest {
	// cannot update SKU; create new product if SKU needs to change
	@Schema(example = "Logitech K380 Keyboard")
	private String name; // optional

	@Schema(example = "Compact multi-device Bluetooth keyboard")
	private String description; // optional

	@Valid
	private MoneyDTO cost; // optional

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public MoneyDTO getCost() {
		return cost;
	}

	public void setCost(MoneyDTO cost) {
		this.cost = cost;
	}

}
