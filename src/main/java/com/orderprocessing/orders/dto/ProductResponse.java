package com.orderprocessing.orders.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Product DTO used to provide information and identification for a specific product.
 */
public class ProductResponse extends AbstractProductDTO {

	@NotBlank
	@Schema(description = "Unique product external identifier",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private String externalId;

	@NotBlank
	@Schema(description = "Unique product identifier; cannot be changed once defined",
			example = "SKU-107435")
	private String sku;

	protected ProductResponse() {}

	public ProductResponse(
						@NotBlank String externalId,
						@NotBlank String sku,
						@NotBlank String name,
						String description,
						boolean active,
						@Valid MoneyDTO cost) {
		super(name, description, active, cost);
		this.externalId = externalId;
		this.sku = sku;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

}
