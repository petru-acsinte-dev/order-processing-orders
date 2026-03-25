package com.orderprocessing.orders.entities;

import java.util.Currency;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CurrencyConverter implements AttributeConverter<Currency, String> {

	@Override
	public String convertToDatabaseColumn(Currency currency) {
		return null == currency ? null : currency.getCurrencyCode();
	}

	@Override
	public Currency convertToEntityAttribute(String symbol) {
		return null == symbol ? null : Currency.getInstance(symbol);
	}

}
