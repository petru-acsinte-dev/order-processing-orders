package com.orderprocessing.orders.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Order DTO used to provide information and contents for a specific order.
 */
public class OrderResponse extends OrderInfo {

	@NotNull
	@Schema(description = "The products added to the order")
	private List<OrderLineDTO> orderLines;

	public List<OrderLineDTO> getOrderLines() {
		return orderLines;
	}

	public void setOrderLines(List<OrderLineDTO> orderLines) {
		this.orderLines = orderLines;
	}

}
