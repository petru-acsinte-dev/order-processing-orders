package com.orderprocessing.orders.dto;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Order line DTO used to represent order lines in {@link OrderResponse}
 */
public class OrderLineDTO {

	@NotNull
	@Schema(description = "Product unique external identifier")
	private UUID productExternalId;

	@NotBlank
	@Schema(description = "Product name")
	private String productName;

	@Valid
	@Schema(description = "Product cost at time of ordering")
	private MoneyDTO cost;

	@Schema(description = "Ordered product quantity")
	private int quantity;

	@Schema(description = "Ordered product total")
	private BigDecimal lineTotal;

	public UUID getProductExternalId() {
		return productExternalId;
	}

	public void setProductExternalId(UUID productExternalId) {
		this.productExternalId = productExternalId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public MoneyDTO getCost() {
		return cost;
	}

	public void setCost(MoneyDTO cost) {
		this.cost = cost;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getLineTotal() {
		return lineTotal;
	}

	public void setLineTotal(BigDecimal lineTotal) {
		this.lineTotal = lineTotal;
	}

}
