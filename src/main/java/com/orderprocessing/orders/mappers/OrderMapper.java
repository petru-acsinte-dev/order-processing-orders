package com.orderprocessing.orders.mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Locale;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.orderprocessing.common.mappers.GlobalMapperConfig;
import com.orderprocessing.orders.dto.MoneyDTO;
import com.orderprocessing.orders.dto.OrderInfo;
import com.orderprocessing.orders.dto.OrderResponse;
import com.orderprocessing.orders.entities.Order;
import com.orderprocessing.orders.entities.OrderLine;

@Mapper(config = GlobalMapperConfig.class, uses = OrderLineMapper.class)
public interface OrderMapper {

	@Mapping(target = "orderTotal", source = "order", qualifiedByName = "mapOrderTotal")
	@Mapping(target = "status", source = "order", qualifiedByName = "mapOrderStatus")
    OrderResponse orderToOrderResponse(Order order);

	@Named("mapOrderStatus")
	default String mapOrderStatus(Order order) {
		return order.getStatus().getStatus();
	}

    @Named("mapOrderTotal")
    default MoneyDTO mapOrderTotal(Order order) {
    	// orders can be empty if products added and later removed
        if (order.getOrderLines().isEmpty()) {
            return new MoneyDTO(BigDecimal.ZERO, Currency.getInstance(Locale.getDefault()));
        }

        // Sum up the line totals
        final BigDecimal totalAmount = order.getOrderLines().stream()
                                      .map(OrderLine::getLineTotal)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return MoneyDTO with total and currency from the first OrderLine
        return new MoneyDTO(totalAmount, order.getOrderLines().get(0).getCost().getCurrency());
    }

    @Mapping(target = "orderTotal", source = "order", qualifiedByName = "mapOrderTotal")
	@Mapping(target = "status", source = "order", qualifiedByName = "mapOrderStatus")
    OrderInfo toInfo(Order order);

    default OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atOffset(ZoneOffset.UTC);
    }

}
