package com.orderprocessing.orders.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.orderprocessing.orders.dto.MoneyDTO;
import com.orderprocessing.orders.dto.OrderLineDTO;
import com.orderprocessing.orders.dto.OrderResponse;
import com.orderprocessing.orders.entities.Money;
import com.orderprocessing.orders.entities.Order;
import com.orderprocessing.orders.entities.OrderLine;
import com.orderprocessing.orders.entities.OrderStatus;
import com.orderprocessing.orders.entities.Product;
import com.orderprocessing.orders.mappers.MoneyMapper;
import com.orderprocessing.orders.mappers.MoneyMapperImpl;
import com.orderprocessing.orders.mappers.OrderLineMapperImpl;
import com.orderprocessing.orders.mappers.OrderMapper;
import com.orderprocessing.orders.mappers.OrderMapperImpl;
import com.orderprocessing.orders.mappers.ProductMapperImpl;

@Tag("mapper")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrderMapperImpl.class, OrderLineMapperImpl.class, ProductMapperImpl.class, MoneyMapperImpl.class})
class OrderMapperTest {

	private static final String CAD = "CAD"; //$NON-NLS-1$

	@Autowired
	private MoneyMapper moneyMapper;

	@Autowired
	private OrderMapper orderMapper;

	@Test
	void testMappers() {
		final Money cad1 = Money.of(BigDecimal.valueOf(212.99D), Currency.getInstance(CAD));
		final Money cad2 = Money.of(BigDecimal.valueOf(111.99D), Currency.getInstance(CAD));
		final List<Money> cad = List.of(cad1, cad2);

		final MoneyDTO cadDTO = moneyMapper.toDTO(cad1);
		assertEquals(cad1.getAmount(), cadDTO.getAmount()); // need exact comparison to confirm mapping does not alter scale
		assertEquals(Currency.getInstance(CAD), cadDTO.getCurrency());

		final Money entity = moneyMapper.toEntity(cadDTO);
		assertEquals(cad1, entity);

		final UUID userExternalId = UUID.fromString("2fa85f64-5717-4562-b3fc-2c963f66afa6"); //$NON-NLS-1$
		final String name1 = "HP LaserJet Pro M404dn"; //$NON-NLS-1$
		final String sku1 = "SKU-100004"; //$NON-NLS-1$
		final Product product1 = new Product(sku1, name1, cad1);
		product1.setActive(true);
		product1.setExternalId(UUID.randomUUID());
		final String name2 = "Samsung 970 EVO Plus 1TB SSD"; //$NON-NLS-1$
		final String sku2 = "SKU-100005"; //$NON-NLS-1$
		final Product product2 = new Product(sku2, name2, cad2);
		product2.setActive(true);
		product2.setExternalId(UUID.randomUUID());

		final Order order = new Order();
		order.setExternalId(UUID.randomUUID());
		final LocalDateTime now = LocalDateTime.now();
		order.setCreated(now);
		final OrderStatus orderStatus = new OrderStatus((short)0, "CREATED"); //$NON-NLS-1$
		order.setStatus(orderStatus);
		order.setCustomerExternalId(userExternalId);
		order.setCost(Money.of(cad1.getAmount().add(cad2.getAmount()), cad1.getCurrency()));

		final OrderLine line1 = new OrderLine();
		line1.setCost(cad1);
		line1.setQuantity(1);
		line1.setLineTotal(cad1.getAmount());
		line1.setProduct(product1);
		line1.setProductName(product1.getName());
		line1.setOrder(order);

		final OrderLine line2 = new OrderLine();
		line2.setCost(cad2);
		line2.setQuantity(2);
		line2.setLineTotal(cad1.getAmount().multiply(BigDecimal.valueOf(line2.getQuantity())));
		line2.setProduct(product2);
		line2.setProductName(product2.getName());
		line2.setOrder(order);

		order.setOrderLines(List.of(line1, line2));

		// to response DTO
		final OrderResponse response = orderMapper.orderToOrderResponse(order);
		assertEquals(now, response.getCreated().toLocalDateTime());
		assertEquals(order.getExternalId(), response.getExternalId());
		assertEquals(userExternalId, response.getCustomerExternalId());
		assertEquals(orderStatus.getStatus(), response.getStatus());
		assertEquals(order.getOrderLines().size(), response.getOrderLines().size());
		assertEquals(line1.getLineTotal().add(line2.getLineTotal()), response.getOrderTotal().getAmount());
		for (int i = 0; i < order.getOrderLines().size(); i++) {
			final OrderLine expectedLine = order.getOrderLines().get(i);
			final OrderLineDTO responseLine = response.getOrderLines().get(i);
			assertEquals(expectedLine.getProduct().getExternalId(), responseLine.getProductExternalId());
			assertEquals(expectedLine.getProduct().getName(), responseLine.getProductName());
			assertEquals(expectedLine.getQuantity(), responseLine.getQuantity());
			assertEquals(expectedLine.getLineTotal(), responseLine.getLineTotal());
			assertEquals(cad.get(i).getAmount(), responseLine.getCost().getAmount());
		}
	}
}
