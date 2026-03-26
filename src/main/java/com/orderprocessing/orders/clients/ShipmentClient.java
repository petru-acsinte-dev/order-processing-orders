package com.orderprocessing.orders.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.orderprocessing.common.dto.CreateFulfillmentRequest;

@FeignClient(name = "shipment", url = "${shipments.service.url}")
public interface ShipmentClient {

	@PostMapping("/fulfillments")
    ResponseEntity<Void> createFulfillment(@RequestBody CreateFulfillmentRequest request);

}
