package com.orderprocessing.orders.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Product DTO used for product creation requests.
 */
public class CreateProductRequest extends AbstractProductDTO {

	@NotBlank
	@Schema(description = "Unique product identifier; cannot be changed once defined",
			example = "SKU-107435")
	private String sku;

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

}
