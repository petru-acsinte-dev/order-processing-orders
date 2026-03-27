package com.orderprocessing.orders.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO used to create or update an order line
 */
public class OrderLineRequest {

	@Schema(description = "Unique product external id",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	@NotNull
	private UUID productId;

	@Schema(description = "Ordered product quantity. Must be positive.",
			example = "2")
	@Min(1)
	private int quantity;

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "OrderLineRequest [productId=" + productId + ", quantity=" + quantity + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
