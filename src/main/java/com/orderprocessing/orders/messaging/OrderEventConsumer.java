package com.orderprocessing.orders.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.orderprocessing.common.configurations.RabbitMQConfig;
import com.orderprocessing.common.constants.Constants;
import com.orderprocessing.common.events.OrderShippedEvent;
import com.orderprocessing.orders.constants.Status;
import com.orderprocessing.orders.services.OrderService;

@Component
public class OrderEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

	private final OrderService orderService;

	public OrderEventConsumer(OrderService orderService) {
		this.orderService = orderService;
	}

	@RabbitListener(queues = RabbitMQConfig.ORDERS_ORDER_SHIPPED_QUEUE)
	public void onOrderShipped(OrderShippedEvent event) {
		final UUID orderId = event.orderExternalId();
		MDC.put(Constants.CORRELATION_ID, event.correlationId());
		try {
			log.info("Received OrderShippedEvent for order {}", orderId); //$NON-NLS-1$
			orderService.updateOrder(orderId, Status.SHIPPED);
		} finally {
			MDC.remove(Constants.CORRELATION_ID);
		}
	}
}
