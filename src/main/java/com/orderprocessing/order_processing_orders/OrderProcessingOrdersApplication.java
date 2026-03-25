package com.orderprocessing.order_processing_orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.orderprocessing")
@EnableJpaRepositories(basePackages = "com.orderprocessing.orders.repositories")
@EntityScan(basePackages = "com.orderprocessing.orders.entities")
public class OrderProcessingOrdersApplication {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		SpringApplication.run(OrderProcessingOrdersApplication.class, args);
	}

}
