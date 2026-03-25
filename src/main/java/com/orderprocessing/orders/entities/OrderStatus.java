package com.orderprocessing.orders.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "status", schema = "orders")
public class OrderStatus {

	@Id
	private Short id;

	@NotBlank
	@Column(name = "status", nullable = false, unique = true)
	private String status;

	protected OrderStatus() {}

	public OrderStatus(Short id, @NotBlank String status) {
		this.id = id;
		this.status = status;
	}

	public Short getId() {
		return id;
	}

	public String getStatus() {
		return status;
	}

	// no setters; immutable
}
