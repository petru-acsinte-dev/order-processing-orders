package com.orderprocessing.orders.controllers;

import static com.orderprocessing.orders.constants.Constants.LOCATION_TEMPLATE;
import static com.orderprocessing.orders.constants.Constants.PRODUCTS_PATH;

import java.net.URI;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.orderprocessing.common.constants.Constants;
import com.orderprocessing.common.response.PagedResponse;
import com.orderprocessing.common.response.ResponseUtils;
import com.orderprocessing.orders.dto.CreateProductRequest;
import com.orderprocessing.orders.dto.ProductResponse;
import com.orderprocessing.orders.dto.UpdateProductRequest;
import com.orderprocessing.orders.services.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Tag (name = "Products controller", description = "Operations related to products management")
@RestController
@RequestMapping(path = PRODUCTS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

	private final ProductService service;

	public ProductController(ProductService service) {
		this.service = service;
	}

	@GetMapping
	@Operation (summary = "Lists products",
				description = "Lists products present in the system.")
	@ApiResponse(responseCode = "200",
				headers = @Header(
		            name = "Link",
		            description = "Pagination links with rel=next and rel=prev",
		            required = false))
	@ApiResponse (responseCode = "403",
				description = "User does not have the required priviledges",
				content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
				description = "Unauthorized user request",
				content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<PagedResponse<ProductResponse>> getAvailableProducts(	@ParameterObject
																				@Parameter(required = false)
																				Pageable pageable) {
		final Page<ProductResponse> page = service.getProducts(pageable);

		return ResponseUtils.getPagedResponse(PRODUCTS_PATH, page);
	}

	@PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@Parameter(name = Constants.PARAM_EXTERNAL_ID, required = true)
	@Operation (summary = "Updates a product",
				description = "Admin operation which updates a product present in the system.")
	@ApiResponse(responseCode = "200",
	content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = ProductResponse.class)))
	@ApiResponse (responseCode = "403",
		description = "User does not have the required priviledges",
		content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
		description = "Unauthorized request",
		content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
		description = "Product not found",
		content = @Content(schema = @Schema(hidden = true)))
	public ProductResponse updateProduct(@RequestParam(required = true) @NotBlank String externalId,
				@Valid @RequestBody UpdateProductRequest updateRequest) {
		final UUID uuid = UUID.fromString(externalId);
		return service.updateProduct(uuid, updateRequest);
	}

	@DeleteMapping(produces = {})
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Parameter(name = Constants.PARAM_EXTERNAL_ID, required = true)
	@Operation (summary = "Marks a product as inactive",
		description = "Admin operation which archives a product present in the system.")
	@ApiResponse(responseCode = "204",
		description = "Product deleted successfully",
		content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "403",
		description = "User does not have the required priviledges",
		content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
		description = "Unauthorized request",
		content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
		description = "Product not found",
		content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<ProductResponse> deleteProduct(@RequestParam(required = true) @NotBlank String externalId) {
		final UUID uuid = UUID.fromString(externalId);
		service.deleteProduct(uuid);
		return ResponseEntity
				.noContent()
				.build();
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation (summary = "Creates a product",
			description = "Admin operation which creates a product in the system.")
	@ApiResponse(responseCode = "201",
	content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
		schema = @Schema(implementation = ProductResponse.class)))
	@ApiResponse (responseCode = "403",
			description = "User does not have the required priviledges",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
			description = "Unauthorized request",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "Product not found",
			content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest createRequest) {
		final ProductResponse newProduct = service.createProduct(createRequest);
		return ResponseEntity
				.created(URI.create(String.format(LOCATION_TEMPLATE, PRODUCTS_PATH, newProduct.getExternalId())))
				.body(newProduct);
	}
}
