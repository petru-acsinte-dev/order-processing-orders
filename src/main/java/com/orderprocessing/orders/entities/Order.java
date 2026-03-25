package com.orderprocessing.orders.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "orders", schema = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "external_id", nullable = false)
	private UUID externalId;

	@NotNull
	@Column(name = "customer_external_id", nullable = false)
	private UUID customerExternalId;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "status_id", referencedColumnName = "id")
	private OrderStatus status;

	@NotNull
	private LocalDateTime created;

	@OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<OrderLine> orderLines = new ArrayList<>();

	@Embedded
	@AttributeOverride(name = "amount", column = @Column(name = "total_amount", precision = 19, scale = 4))
	private Money cost;

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

	public UUID getCustomerExternalId() {
		return customerExternalId;
	}

	public void setCustomerExternalId(UUID customerExternalId) {
		this.customerExternalId = customerExternalId;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public Money getCost() {
		return cost;
	}

	public void setCost(Money cost) {
		this.cost = cost;
	}

	public List<OrderLine> getOrderLines() {
		return orderLines;
	}

	public void setOrderLines(List<OrderLine> orderLines) {
		this.orderLines = orderLines;
	}

}
