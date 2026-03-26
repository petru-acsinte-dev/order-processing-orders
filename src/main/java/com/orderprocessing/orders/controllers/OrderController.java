package com.orderprocessing.orders.controllers;

import static com.orderprocessing.orders.constants.Constants.ORDERS_PATH;

import java.net.URI;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.orderprocessing.common.response.PagedResponse;
import com.orderprocessing.common.response.ResponseUtils;
import com.orderprocessing.orders.constants.Constants;
import com.orderprocessing.orders.constants.Status;
import com.orderprocessing.orders.dto.CreateOrderRequest;
import com.orderprocessing.orders.dto.OrderInfo;
import com.orderprocessing.orders.dto.OrderResponse;
import com.orderprocessing.orders.dto.UpdateOrderRequest;
import com.orderprocessing.orders.services.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag (name = "Orders controller", description = "Operations related to product orders")
@RestController
@RequestMapping(path = ORDERS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

	private final OrderService service;

	public OrderController(OrderService service) {
		this.service = service;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation (summary = "Creates an order",
			description = "Creates an order in the system containing the submitted products and quantities.")
	@ApiResponse(responseCode = "201",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = OrderResponse.class)))
	@ApiResponse (responseCode = "400",
			description = "Bad, incomplete, or request is too big",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "Product not found",
			content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest createRequest) {
		final OrderResponse newOrder = service.createOrder(createRequest);
		return ResponseEntity
				.created(URI.create(String.format(Constants.LOCATION_TEMPLATE,
												ORDERS_PATH,
												newOrder.getExternalId())))
				.body(newOrder);
	}

	@PatchMapping(path = "/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation (summary = "Updates an order",
			description = "Adds, removes and changes product quantities in an existing order.")
	@ApiResponse(responseCode = "200",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = OrderResponse.class)))
	@ApiResponse (responseCode = "400",
			description = "Bad, incomplete, or request is too big",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "Order not found",
			content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<OrderResponse> updateOrder(
			@PathVariable UUID orderId,
			@Valid @RequestBody UpdateOrderRequest updateRequest) {
		final OrderResponse changedOrder = service.updateOrder(orderId, updateRequest);
		return ResponseEntity
				.ok(changedOrder);
	}

	@PostMapping("/{orderId}/cancel")
	@Operation (summary = "Cancels an order",
			description = "Cancels an existing order.")
	@ApiResponse(responseCode = "200",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = OrderInfo.class)))
	@ApiResponse (responseCode = "403",
			description = "The user does not have permissions to cancel the order",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "Order not found",
			content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<OrderInfo> cancelOrder(@PathVariable UUID orderId) {
		final OrderInfo updatedOrder = service.updateOrder(orderId, Status.CANCELLED);
		return ResponseEntity.ok(updatedOrder);
	}

	@PostMapping("/{orderId}/confirm")
	@Operation (summary = "Confirms an order",
			description = "Confirms an existing draft order.")
	@ApiResponse(responseCode = "200",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = OrderInfo.class)))
	@ApiResponse (responseCode = "403",
			description = "The user does not have permissions to confirm the order",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "Order not found",
			content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<OrderInfo> confirmOrder(@PathVariable UUID orderId) {
		final OrderInfo updatedOrder = service.updateOrder(orderId, Status.CONFIRMED);
		return ResponseEntity.ok(updatedOrder);
	}

	@PostMapping("/{orderId}/ship")  // TODO: to be used only by inter-service calls
	@Operation (summary = "Marks an order as shipped",
			description = "Marks an existing order as shipped.")
	@ApiResponse(responseCode = "200",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = OrderInfo.class)))
	@ApiResponse (responseCode = "403",
			description = "The user does not have permissions to mark the order as shipped",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "Order not found",
			content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<OrderInfo> shipOrder(@PathVariable UUID orderId) {
		final OrderInfo updatedOrder = service.updateOrder(orderId, Status.SHIPPED);
		return ResponseEntity.ok(updatedOrder);
	}

	@GetMapping("/{orderId}")
	@Operation (summary = "Retrieves an order",
			description = "Retrieves an order and its contents.")
	@ApiResponse(responseCode = "200",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = OrderResponse.class)))
	@ApiResponse (responseCode = "403",
			description = "User does not have access to the specified order",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "Order not found",
			content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
		final OrderResponse order = service.getOrder(orderId);
		return ResponseEntity.ok(order);
	}

	@GetMapping
	@Operation (summary = "Lists orders",
				description = "Lists available orders, newest first")
	@ApiResponse(responseCode = "200",
				headers = @Header(
		            name = "Link",
		            description = "Pagination links with rel=next and rel=prev",
		            required = false))
	@ApiResponse (responseCode = "403",
				description = "User does not have the required priviledges",
				content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
				description = "The specified optional owner was not found",
				content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<PagedResponse<OrderInfo>> getOrders(
			@RequestParam(required = false) UUID ownerId,
			@ParameterObject @Parameter(required = false) Pageable pageable) {
		final var page = service.getOrders(ownerId, pageable);

		return ResponseUtils.getPagedResponse(ORDERS_PATH, page);
	}
}
