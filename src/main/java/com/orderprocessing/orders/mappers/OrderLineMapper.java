package com.orderprocessing.orders.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.orderprocessing.common.mappers.GlobalMapperConfig;
import com.orderprocessing.orders.dto.OrderLineDTO;
import com.orderprocessing.orders.entities.OrderLine;

@Mapper(config = GlobalMapperConfig.class)
public interface OrderLineMapper extends GlobalMapperConfig {

	@Mapping(target = "productExternalId", source = "product.externalId")
	OrderLineDTO toLineDTO(OrderLine line);

}
