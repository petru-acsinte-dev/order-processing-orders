package com.orderprocessing.orders.dto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class MoneyDTO {

	@NotNull
	@PositiveOrZero
	private final BigDecimal amount;

	@NotNull
	@Schema(description = "ISO 4217 currency symbol", example = "USD, CAD, EUR etc.")
	private final Currency currency;

	public MoneyDTO(BigDecimal amount, Currency currency) {
		this.amount = amount;
		this.currency = currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Currency getCurrency() {
		return currency;
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
		final MoneyDTO other = (MoneyDTO) obj;
		final boolean comparableCurrency = (null == this.currency) == (null == other.currency);
		if (! comparableCurrency) {
			return false;
		}
		return Objects.equals(amount, other.amount)
				&& Objects.equals(currency.getCurrencyCode(), other.currency.getCurrencyCode());
	}

	@Override
	public String toString() {
		return "MoneyDTO [amount=" + amount + ", currency=" + currency + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
