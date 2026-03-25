package com.orderprocessing.orders.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.orderprocessing.common.mappers.GlobalMapperConfig;
import com.orderprocessing.orders.dto.MoneyDTO;
import com.orderprocessing.orders.entities.Money;

@Mapper(config = GlobalMapperConfig.class)
public interface MoneyMapper extends GlobalMapperConfig, MoneyFactory {

	@Mapping(target = "add", ignore = true)
	@Mapping(target = "multiply", ignore = true)
	Money toEntity(MoneyDTO dto);

	MoneyDTO toDTO(Money entity);

}
