package com.orderprocessing.orders.entities;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

import com.orderprocessing.orders.exceptions.NonMatchingCurrencyException;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Embeddable
public class Money {

	@NotNull
	@PositiveOrZero
	@Column(name = "amount", precision = 19, scale = 4)
	private BigDecimal amount;

	@NotNull
	@Column(name = "currency", length = 3)
	@Convert(converter = CurrencyConverter.class)
	private Currency currency; // ISO 4217 code

	private Money() {}

	private Money(@NotNull @PositiveOrZero BigDecimal amount, @NotNull Currency currency) {
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Amount cannot be negative"); //$NON-NLS-1$
		}
		this.amount = amount;
		this.currency = currency;
	}

	public static Money of(BigDecimal amount, Currency currency) {
		return new Money(amount, currency);
	}

	// no setters; immutable

	public BigDecimal getAmount() {
		return amount;
	}

	public Currency getCurrency() {
		return currency;
	}

	public Money add(Money money) {
		checkCurrency(money);
		return Money.of(amount.add(money.amount), currency);
	}

	public Money multiply(Money money) {
		checkCurrency(money);
		return Money.of(amount.multiply(money.getAmount()), currency);
	}

	private void checkCurrency(Money money) {
		if (null == money) {
			throw new IllegalArgumentException("Money cannot be null"); //$NON-NLS-1$
		}
		if (currency.equals(money.getCurrency())) {
			return;
		}
		throw new NonMatchingCurrencyException(currency.getCurrencyCode(), money.currency.getCurrencyCode());
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, currency);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		final Money other = (Money) obj;
		return Objects.equals(amount, other.amount) && Objects.equals(currency, other.currency);
	}

}
