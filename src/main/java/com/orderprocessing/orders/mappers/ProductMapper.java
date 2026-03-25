package com.orderprocessing.orders.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.orderprocessing.common.mappers.GlobalMapperConfig;
import com.orderprocessing.orders.dto.CreateProductRequest;
import com.orderprocessing.orders.dto.ProductResponse;
import com.orderprocessing.orders.dto.UpdateProductRequest;
import com.orderprocessing.orders.entities.Product;

@Mapper(config = GlobalMapperConfig.class)
public interface ProductMapper extends GlobalMapperConfig, MoneyFactory {

	ProductResponse toResponse(Product entity);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "externalId", ignore = true)
	@Mapping(target = "active", ignore = true) // always true upon creation
	@Mapping(target = "cost.add", ignore = true)
	@Mapping(target = "cost.multiply", ignore = true)
	Product toEntity(CreateProductRequest dto);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "externalId", ignore = true)
	@Mapping(target = "sku", ignore = true) // cannot update SKU
	@Mapping(target = "active", ignore = true) // only updated through deletion
	@Mapping(target = "cost.add", ignore = true)
	@Mapping(target = "cost.multiply", ignore = true)
	Product toEntity(UpdateProductRequest dto);

}
