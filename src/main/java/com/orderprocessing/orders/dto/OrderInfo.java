package com.orderprocessing.orders.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embedded;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Order DTO used to provide basic information and identification for a specific order.
 */
public class OrderInfo {

	@NotNull
	@Schema(description = "The order unique external identifier",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID externalId;

	@NotNull
	@Schema(description = "The unique external identifier for the order owner",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID customerExternalId;

	@NotBlank
	@Schema(description = "The order status",
			example = "One of CREATED, CANCELLED, CONFIRMED, SHIPPED")
	private String status;

	@NotNull
	@Schema(description = "The order creation date")
	private OffsetDateTime created;

	@Embedded
	private MoneyDTO orderTotal;

	public UUID getExternalId() {
		return externalId;
	}

	public void setExternalId(UUID externalId) {
		this.externalId = externalId;
	}

	public UUID getCustomerExternalId() {
		return customerExternalId;
	}

	public void setCustomerExternalId(UUID customerExternalId) {
		this.customerExternalId = customerExternalId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public MoneyDTO getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(MoneyDTO orderTotal) {
		this.orderTotal = orderTotal;
	}

}
