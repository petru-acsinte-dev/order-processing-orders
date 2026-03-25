package com.orderprocessing.orders.mapper;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.orderprocessing.orders.dto.CreateProductRequest;
import com.orderprocessing.orders.dto.MoneyDTO;
import com.orderprocessing.orders.dto.ProductResponse;
import com.orderprocessing.orders.dto.UpdateProductRequest;
import com.orderprocessing.orders.entities.Money;
import com.orderprocessing.orders.entities.Product;
import com.orderprocessing.orders.mappers.MoneyMapper;
import com.orderprocessing.orders.mappers.MoneyMapperImpl;
import com.orderprocessing.orders.mappers.ProductMapper;
import com.orderprocessing.orders.mappers.ProductMapperImpl;

@Tag("mapper")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MoneyMapperImpl.class, ProductMapperImpl.class})
class MoneyMapperTest {

	private static final String CAD = "CAD"; //$NON-NLS-1$

	@Autowired
	private MoneyMapper moneyMapper;

	@Autowired
	private ProductMapper productMapper;

	@Test
	void testMappers() {
		final Money cad = Money.of(BigDecimal.valueOf(12.99D), Currency.getInstance(CAD));

		final MoneyDTO cadDTO = moneyMapper.toDTO(cad);
		assertEquals(cad.getAmount(), cadDTO.getAmount()); // need exact comparison to confirm mapping does not alter scale
		assertEquals(Currency.getInstance(CAD), cadDTO.getCurrency());

		final Money entity = moneyMapper.toEntity(cadDTO);
		assertEquals(cad, entity);

		final UUID uuid = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"); //$NON-NLS-1$
		final String name = "HP LaserJet Pro M404dn"; //$NON-NLS-1$
		final String sku = "SKU-100004"; //$NON-NLS-1$
		final String description = "Monochrome laser printer with duplex printing"; //$NON-NLS-1$

		{ // to DTO
			final Product product = new Product(sku, name, cad);
			product.setDescription(description);
			product.setExternalId(uuid);

			final ProductResponse response = productMapper.toResponse(product);
			assertEquals(cadDTO, response.getCost());
			assertEquals(name, response.getName());
			assertEquals(uuid, UUID.fromString(response.getExternalId()));
			assertEquals(sku, response.getSku());
			assertEquals(description, response.getDescription());
		}

		{ // to entity
			final CreateProductRequest create = new CreateProductRequest();
			create.setName(name);
			create.setSku(sku);
			create.setDescription(description);
			create.setCost(cadDTO);

			final Product newProduct = productMapper.toEntity(create);
			assertEquals(name, newProduct.getName());
			assertEquals(description, newProduct.getDescription());
			assertEquals(sku, newProduct.getSku());
			assertEquals(cad, newProduct.getCost());
		}

		{ // to entity
			final UpdateProductRequest update = new UpdateProductRequest();
			update.setName(name);
			update.setDescription(description);
			update.setCost(cadDTO);

			final Product changedProduct = productMapper.toEntity(update);
			assertEquals(name, changedProduct.getName());
			assertEquals(description, changedProduct.getDescription());
			assertEquals(cad, changedProduct.getCost());
			assertNull(changedProduct.getSku());
		}
	}
}
