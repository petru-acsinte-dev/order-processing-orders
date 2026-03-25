package com.orderprocessing.orders.mappers;

import org.mapstruct.ObjectFactory;

import com.orderprocessing.orders.dto.MoneyDTO;
import com.orderprocessing.orders.entities.Money;

interface MoneyFactory {

	@ObjectFactory
	default Money toMoney(MoneyDTO dto) {
		if (null == dto) {
			return null;
		}
		return Money.of(dto.getAmount(), dto.getCurrency());
	}

}
