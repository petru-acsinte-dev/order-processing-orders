package com.orderprocessing.orders.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public abstract class AbstractProductDTO {

	@NotBlank
	@Schema(example = "Logitech K380 Keyboard")
	private String name;

	@Schema(example = "Compact multi-device Bluetooth keyboard")
	private String description;

	@Schema(description = "Indicates if the product is active and not discontinued")
	private boolean active;

	@Valid
	private MoneyDTO cost;

	protected AbstractProductDTO() {}

	protected AbstractProductDTO(@NotBlank String name, String description, boolean active, @Valid MoneyDTO cost) {
		this.name = name;
		this.description = description;
		this.active = active;
		this.cost = cost;
	}

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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public MoneyDTO getCost() {
		return cost;
	}

	public void setCost(MoneyDTO cost) {
		this.cost = cost;
	}

}
