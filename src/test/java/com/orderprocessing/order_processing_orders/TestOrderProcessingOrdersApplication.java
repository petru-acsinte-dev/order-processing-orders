package com.orderprocessing.order_processing_orders;

import org.springframework.boot.SpringApplication;

public class TestOrderProcessingOrdersApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrderProcessingOrdersApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
