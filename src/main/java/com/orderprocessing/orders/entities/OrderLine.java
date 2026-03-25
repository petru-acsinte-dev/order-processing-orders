package com.orderprocessing.orders.entities;

import java.math.BigDecimal;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "order_lines", schema = "orders")
public class OrderLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", referencedColumnName = "id")
	private Order order;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	private Product product;

	@NotBlank
	@Column(name = "product_name", nullable = false)
	private String productName;

	@NotNull
	@Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price", precision = 19, scale = 4))
	private Money cost;

	@NotNull
	@PositiveOrZero
	private int quantity;

	@NotNull
	@PositiveOrZero
	@Column(name = "line_total", precision = 19, scale = 4)
	private BigDecimal lineTotal;

	@PrePersist
	@PreUpdate
	private void copyProductInfo() {
		if (null != product) {
			productName = product.getName();
			cost = product.getCost();
		}
		recalculateTotal();
	}

	// calculates
	private void recalculateTotal() {
		if (null != cost && quantity > 0) {
			lineTotal = cost.getAmount().multiply(BigDecimal.valueOf(quantity));
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Money getCost() {
		return cost;
	}

	public void setCost(Money cost) {
		this.cost = cost;
		recalculateTotal();
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quatity) {
		this.quantity = quatity;
		recalculateTotal();
	}

	public BigDecimal getLineTotal() {
		return lineTotal;
	}

	public void setLineTotal(BigDecimal lineTotal) {
		this.lineTotal = lineTotal;
	}

}
