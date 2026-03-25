package com.orderprocessing.orders.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "products", schema = "orders")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "external_id", nullable = false)
	private UUID externalId;

	@NotNull
	@Column(name = "sku", nullable = false)
	private String sku;

	@NotNull
	@Column(name = "name", nullable = false)
	private String name;

	private String description;

	boolean active;

	@Embedded
	Money cost;

	public Product() {} // used by tests mocks; hence not protected

	/**
	 * @param sku
	 * @param name
	 * @param cost
	 */
	public Product(	@NotBlank String sku,
					@NotBlank String name,
					@Valid Money cost) {
		this.sku = sku;
		this.name = name;
		this.cost = cost;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UUID getExternalId() {
		return externalId;
	}

	public void setExternalId(UUID externalId) {
		this.externalId = externalId;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
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

	public Money getCost() {
		return cost;
	}

	public void setCost(Money cost) {
		this.cost = cost;
	}

}
