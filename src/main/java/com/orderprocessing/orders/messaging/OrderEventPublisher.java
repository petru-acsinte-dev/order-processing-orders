package com.orderprocessing.orders.messaging;

import static com.orderprocessing.common.configurations.RabbitMQConfig.EXCHANGE;
import static com.orderprocessing.common.configurations.RabbitMQConfig.ORDER_CONFIRMED_KEY;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.orderprocessing.common.constants.Constants;
import com.orderprocessing.common.events.OrderConfirmedEvent;

@Component
public class OrderEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

	private final RabbitTemplate rabbitTemplate;

	public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publishOrderConfirmed(UUID externalOrderId) {
		final OrderConfirmedEvent confirmedEvent =
				new OrderConfirmedEvent(externalOrderId,
										Instant.now(),
										MDC.get(Constants.CORRELATION_ID));
		log.info("Publishing confirmed order for {}", externalOrderId); //$NON-NLS-1$
		rabbitTemplate.convertAndSend(EXCHANGE, ORDER_CONFIRMED_KEY, confirmedEvent);
	}

}
