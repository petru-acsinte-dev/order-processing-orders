package com.orderprocessing.orders.integration;

import static com.orderprocessing.common.tests.TestConstants.ADMIN;
import static com.orderprocessing.orders.constants.Constants.ORDERS_PATH;
import static com.orderprocessing.orders.constants.Constants.PRODUCTS_PATH;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.orderprocessing.common.constants.Constants;
import com.orderprocessing.common.tests.AbstractIntegrationTestBase;
import com.orderprocessing.order_processing_orders.OrderProcessingOrdersApplication;
import com.orderprocessing.orders.constants.Status;
import com.orderprocessing.orders.dto.CreateOrderRequest;
import com.orderprocessing.orders.dto.OrderInfo;
import com.orderprocessing.orders.dto.OrderLineRequest;
import com.orderprocessing.orders.dto.OrderResponse;
import com.orderprocessing.orders.dto.ProductResponse;
import com.orderprocessing.orders.dto.UpdateOrderRequest;

import io.jsonwebtoken.lang.Collections;

@SpringBootTest(classes = OrderProcessingOrdersApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles(value={"test"})
@Transactional
class OrderIT extends AbstractIntegrationTestBase {

	private final Logger log = LoggerFactory.getLogger(OrderIT.class);

	private static final String MEMBR_TMPLT = "$.%s"; //$NON-NLS-1$
	private static final String MEMBR_TOTAL_TMPLT = "$.orderTotal.%s"; //$NON-NLS-1$
	private static final String CNT_ARRAY_MEMBR_TMPLT = "$.%s[%d].%s"; //$NON-NLS-1$
	private static final String CNT_ARRAY_MEMBR_COST_TMPLT = "$.%s[%d].cost.%s"; //$NON-NLS-1$
	private static final String FIELD_EXTERNAL_ID = "externalId"; //$NON-NLS-1$
	private static final String FIELD_EXTERNAL_USER_ID = "customerExternalId"; //$NON-NLS-1$
	private static final String FIELD_EXTERNAL_PRODUCT_ID = "productExternalId"; //$NON-NLS-1$
	private static final String FIELD_STATUS = "status"; //$NON-NLS-1$
	private static final String FIELD_AMOUNT = "amount"; //$NON-NLS-1$
	private static final String FIELD_CURRENCY = "currency"; //$NON-NLS-1$
	private static final String FIELD_ORDER_LINES = "orderLines"; //$NON-NLS-1$
	private static final String FIELD_PRODUCT_NAME = "productName"; //$NON-NLS-1$
	private static final String FIELD_QUANTITY = "quantity"; //$NON-NLS-1$
	private static final String FIELD_LINE_TOTAL = "lineTotal"; //$NON-NLS-1$

	private final UUID userExternalId = UUID.randomUUID();

	@Value("${jwt.secret}")
	private String secret;

	@BeforeEach
	void login() {
		mockLogin(TEST_USER, userExternalId, Constants.USER_ROLE, secret);
	}

	@Override
	protected Logger getLog() {
		return log;
	}

	@Test
	@DisplayName("Tests adding a product to an order and changing quantity for another")
	@Rollback
	void testUpdateOrder() throws Exception {
		// selecting a random product
		final List<ProductResponse> products = getProducts();
		final Random random = new Random();
		final ProductResponse product = products.get(random.nextInt(ProductIT.PAGE_SIZE));

		final int quantity = 2;
		final ResultActions creationActions = doCreateOrder(List.of(Pair.of(product, quantity)));
		creationActions
			.andExpect(status().isCreated())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(Status.CREATED.name()));

		final MvcResult created = creationActions.andReturn();
		final String orderLocation = created.getResponse().getHeader(HttpHeaders.LOCATION);
		final OrderResponse newOrder = objectMapper.readValue(created.getResponse().getContentAsString(), OrderResponse.class);
		final UUID productUUID = newOrder.getOrderLines().get(0).getProductExternalId();

		// new product to add
		ProductResponse newProductToAdd = products.get(random.nextInt(ProductIT.PAGE_SIZE));
		// making sure the same product was not selected twice
		while(product.getExternalId().equals(newProductToAdd.getExternalId())
				|| (! product.getCost().getCurrency().getCurrencyCode()
						.equals(newProductToAdd.getCost().getCurrency().getCurrencyCode()))) {
			// pick a different product with matching currency
			newProductToAdd = products.get(random.nextInt(ProductIT.PAGE_SIZE));
		}
		assertNotEquals(product.getExternalId(), newProductToAdd.getExternalId());

		// changing the quantity for the product
		final OrderLineRequest changeLineRequest = new OrderLineRequest();
		changeLineRequest.setProductId(productUUID);
		changeLineRequest.setQuantity(quantity * 2);

		// adding new product
		final OrderLineRequest newLineRequest = new OrderLineRequest();
		newLineRequest.setProductId(UUID.fromString(newProductToAdd.getExternalId()));
		newLineRequest.setQuantity(1);

		// preparing the update request
		final UpdateOrderRequest updateRequest = new UpdateOrderRequest();
		updateRequest.setUpsertProducts(List.of(changeLineRequest, newLineRequest));

		final ResultActions updateActions = mockMvc.perform(patch(orderLocation)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(updateRequest)));

		updateActions.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID).value(newOrder.getExternalId().toString()))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_USER_ID).value(matchesPattern(UUID_REGEX))) // customerExternalId
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_USER_ID).value(userExternalId.toString()))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(Status.CREATED.name())) // status
				.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_AMOUNT).isNumber())
				.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_AMOUNT)
						.value(
								closeTo(product.getCost().getAmount()
									.multiply(BigDecimal.valueOf(quantity * 2))
									.add(newProductToAdd.getCost().getAmount()).doubleValue(), 0.001))) // orderTotal.amount
				.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_CURRENCY).isNotEmpty())
				.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_CURRENCY)
						.value(product.getCost().getCurrency().getCurrencyCode()))
				.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_CURRENCY)
						.value(newProductToAdd.getCost().getCurrency().getCurrencyCode())); // orderTotal.currency

		// OrderLineDTO
		updateActions.andExpect(jsonPath(MEMBR_TMPLT, FIELD_ORDER_LINES).isArray())
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_ORDER_LINES).value(hasSize(2)))
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_EXTERNAL_PRODUCT_ID)
						.value(product.getExternalId())) // productExternalId
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_PRODUCT_NAME)
						.value(product.getName())) // productName
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_QUANTITY)
						.value(quantity * 2)) // quantity
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_LINE_TOTAL)
						.value(
						// needed to match BigDecimals with different scales
						closeTo(product.getCost().getAmount().multiply(BigDecimal.valueOf(quantity * 2)).doubleValue(), 0.001))) // lineTotal
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, FIELD_ORDER_LINES, 0, FIELD_AMOUNT)
						.value(product.getCost().getAmount())) // cost.amount
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, FIELD_ORDER_LINES, 0, FIELD_CURRENCY)
						.value(product.getCost().getCurrency().getCurrencyCode())) // cost.currency
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 1, FIELD_EXTERNAL_PRODUCT_ID)
						.value(newProductToAdd.getExternalId())) // productExternalId
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 1, FIELD_PRODUCT_NAME)
						.value(newProductToAdd.getName())) // productName
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 1, FIELD_QUANTITY).value(1)) // quantity
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 1, FIELD_LINE_TOTAL)
						.value(newProductToAdd.getCost().getAmount())) // lineTotal
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, FIELD_ORDER_LINES, 1, FIELD_AMOUNT)
						.value(newProductToAdd.getCost().getAmount())) // cost.amount
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, FIELD_ORDER_LINES, 1, FIELD_CURRENCY)
						.value(newProductToAdd.getCost().getCurrency().getCurrencyCode())); // cost.currency
	}

	@Test
	@DisplayName("Tests removing products from an order")
	@Rollback
	/**
	 * The test creates an order with 5 products and removes 4 of them.
	 * Checks that only one correct product remains.
	 * @throws Exception
	 */
	void testRemoveFromOrder() throws Exception {
		// selecting a random product
		final List<ProductResponse> products = getProducts();
		final ProductResponse product0 = products.get(0);
		final ProductResponse product1 = products.get(1);
		final ProductResponse product2 = products.get(2);
		final ProductResponse product3 = products.get(3);
		final ProductResponse product4 = products.get(4);

		final int quantity = 1;
		final ResultActions creationActions = doCreateOrder(List.of(Pair.of(product0, quantity),
																	Pair.of(product1, quantity * 2),
																	Pair.of(product2, quantity * 3),
																	Pair.of(product3, quantity * 4),
																	Pair.of(product4, quantity * 5)));
		creationActions
			.andExpect(status().isCreated())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(Status.CREATED.name()));

		final MvcResult created = creationActions.andReturn();
		final String orderLocation = created.getResponse().getHeader(HttpHeaders.LOCATION);
		final OrderResponse newOrder = objectMapper.readValue(created.getResponse().getContentAsString(), OrderResponse.class);

		// preparing the update request
		final UpdateOrderRequest updateRequest = new UpdateOrderRequest();
		updateRequest.setRemovedProducts(List.of(UUID.fromString(product1.getExternalId()),
												UUID.fromString(product2.getExternalId()),
												UUID.fromString(product3.getExternalId()),
												UUID.fromString(product4.getExternalId())));

		final ResultActions updateActions = mockMvc.perform(patch(orderLocation)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(updateRequest)));

		updateActions.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID).value(newOrder.getExternalId().toString()))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_USER_ID).value(matchesPattern(UUID_REGEX))) // customerExternalId
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_USER_ID).value(userExternalId.toString()))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(Status.CREATED.name())) // status
				.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_AMOUNT).isNumber())
				.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_AMOUNT)
						.value(product0.getCost().getAmount())) // orderTotal.amount
				.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_CURRENCY).isNotEmpty())
				.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_CURRENCY)
						.value(product0.getCost().getCurrency().getCurrencyCode()))
				.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_CURRENCY)
						.value(product1.getCost().getCurrency().getCurrencyCode())); // orderTotal.currency

		// OrderLineDTO
		updateActions.andExpect(jsonPath(MEMBR_TMPLT, FIELD_ORDER_LINES).isArray())
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_ORDER_LINES).value(hasSize(1)))
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_EXTERNAL_PRODUCT_ID)
						.value(product0.getExternalId())) // productExternalId
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_PRODUCT_NAME)
						.value(product0.getName())) // productName
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_QUANTITY)
						.value(quantity)) // quantity
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_LINE_TOTAL)
						.value(
						// needed to match BigDecimals with different scales
						closeTo(product0.getCost().getAmount().multiply(BigDecimal.valueOf(quantity)).doubleValue(), 0.001))) // lineTotal
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, FIELD_ORDER_LINES, 0, FIELD_AMOUNT)
						.value(product0.getCost().getAmount())) // cost.amount
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, FIELD_ORDER_LINES, 0, FIELD_CURRENCY)
						.value(product0.getCost().getCurrency().getCurrencyCode())); // cost.currency
	}

	@Test
	@DisplayName("Tests creating an order with a single product")
	@Rollback
	void testCreateOrder() throws Exception {
		// selecting a random product
		final List<ProductResponse> products = getProducts();
		final Random random = new Random();
		final ProductResponse product = products.get(random.nextInt(ProductIT.PAGE_SIZE));

		doCreateOrder(Collections.emptyList())
			.andExpect(status().isBadRequest());

		final int quantity = 2;
		final ResultActions resultActions = doCreateOrder(List.of(Pair.of(product, quantity)));

		resultActions
			.andExpect(status().isCreated())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID).isNotEmpty()) // externalId
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID)
					.value(matchesPattern(UUID_REGEX)))
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_USER_ID)
					.value(matchesPattern(UUID_REGEX))) // customerExternalId
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_USER_ID)
					.value(userExternalId.toString()))
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS)
					.value(Status.CREATED.name())) // status
			.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_AMOUNT).isNumber())
			.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_AMOUNT)
					.value(closeTo(product.getCost().getAmount().multiply(BigDecimal.valueOf(quantity)).doubleValue(), 0.001))) // orderTotal.amount
			.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_CURRENCY).isNotEmpty())
			.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_CURRENCY)
					.value(product.getCost().getCurrency().getCurrencyCode())); // orderTotal.currency

		// OrderLineDTO
		resultActions
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_ORDER_LINES).isArray())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_ORDER_LINES).isNotEmpty())
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_EXTERNAL_PRODUCT_ID)
					.value(product.getExternalId())) // productExternalId
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_PRODUCT_NAME)
					.value(product.getName())) // productName
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_QUANTITY)
					.value(quantity)) // quantity
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_LINE_TOTAL)
					.value(product.getCost().getAmount().multiply(BigDecimal.valueOf(quantity)))) // lineTotal
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, FIELD_ORDER_LINES, 0, FIELD_AMOUNT)
					.value(product.getCost().getAmount()))  //cost.amount
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, FIELD_ORDER_LINES, 0, FIELD_CURRENCY)
					.value(product.getCost().getCurrency().getCurrencyCode())); // cost.currency
	}

	@Test
	@DisplayName("Tests changing the status for an order")
	@Rollback
	void testChangeOrderStatus() throws Exception {
		// selecting a random product
		final List<ProductResponse> products = getProducts();
		final Random random = new Random();
		final ProductResponse product = products.get(random.nextInt(ProductIT.PAGE_SIZE));

		final int quantity = 2;
		final ResultActions creationActions = doCreateOrder(List.of(Pair.of(product, quantity)));

		creationActions.andExpect(status().isCreated());

		final MvcResult created = creationActions.andReturn();
		final String orderLocation = created.getResponse().getHeader(HttpHeaders.LOCATION);

		// should not be able to change to shipped
		cannotShip(orderLocation);

		// should be able to confirm
		confirm(orderLocation);

		// should be able to cancel (not yet shipped)
		cancel(orderLocation);
	}

	@Test
	@DisplayName("Tests retrieving the placed orders")
	@Rollback
	void testGetOrders() throws Exception {
		// selecting a random product
		final List<ProductResponse> products = getProducts();
		final Random random = new Random();
		ProductResponse product = products.get(random.nextInt(ProductIT.PAGE_SIZE));

		mockLogin(ADMIN, Constants.ADMIN_UUID0, Constants.ADMIN_ROLE, secret);

		ResultActions creationActions = doCreateOrder(List.of(Pair.of(product, 1)));
		creationActions.andExpect(status().isCreated());

		mockLogin(TEST_USER, userExternalId, Constants.USER_ROLE, secret);

		product = products.get(random.nextInt(ProductIT.PAGE_SIZE));
		final int quantity = 2;
		creationActions = doCreateOrder(List.of(Pair.of(product, quantity)));
		creationActions.andExpect(status().isCreated());

		MvcResult created = creationActions.andReturn();
		final String order1Location = created.getResponse().getHeader(HttpHeaders.LOCATION);

		// should be able to confirm
		confirm(order1Location);

		product = products.get(random.nextInt(ProductIT.PAGE_SIZE));
		creationActions = doCreateOrder(List.of(Pair.of(product, quantity)));
		creationActions.andExpect(status().isCreated());

		created = creationActions.andReturn();
		final String order2Location = created.getResponse().getHeader(HttpHeaders.LOCATION);

		// should be able to cancel (not yet shipped)
		cancel(order2Location);

		MvcResult result = mockMvc.perform(get(ORDERS_PATH)
					.accept(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, getBearer()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR).isArray()) //$NON-NLS-1$
			.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR + ".length()").value(2)) //$NON-NLS-1$ //$NON-NLS-2$
			.andReturn();
		JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
		JsonNode contentNode = root.get(Constants.PAGE_CONTENT_ATTR);
		List<OrderInfo> contents = objectMapper.convertValue(contentNode, new TypeReference<List<OrderInfo>>() {});

		for (final OrderInfo orderResponse : contents) {
			if (order1Location.endsWith(orderResponse.getExternalId().toString())) {
				assertEquals(Status.CONFIRMED.name(), orderResponse.getStatus());
				continue;
			}
			assertEquals(Status.CANCELLED.name(), orderResponse.getStatus());
		}

		mockLogin(ADMIN, Constants.ADMIN_UUID0, Constants.ADMIN_ROLE, secret);

		result = mockMvc.perform(get(ORDERS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR).isArray()) //$NON-NLS-1$
			.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR + ".length()").value(3)) //$NON-NLS-1$ //$NON-NLS-2$
			.andReturn();
		root = objectMapper.readTree(result.getResponse().getContentAsString());
		contentNode = root.get(Constants.PAGE_CONTENT_ATTR);
		contents = objectMapper.convertValue(contentNode, new TypeReference<List<OrderInfo>>() {});

		for (final OrderInfo orderResponse : contents) {
			if (order1Location.endsWith(orderResponse.getExternalId().toString())) {
				assertEquals(Status.CONFIRMED.name(), orderResponse.getStatus());
			} else if (order2Location.endsWith(orderResponse.getExternalId().toString())) {
				assertEquals(Status.CANCELLED.name(), orderResponse.getStatus());
			} else {
				assertEquals(Status.CREATED.name(), orderResponse.getStatus());
			}
		}
	}

	@Test
	@DisplayName("Tests creating an order with a non-existent product")
	@Rollback
	void testCreateOrderForMissingProduct() throws Exception {
		// selecting a random product
		final CreateOrderRequest createRequest = new CreateOrderRequest();
		final OrderLineRequest lineRequest = new OrderLineRequest();
		lineRequest.setProductId(UUID.randomUUID());
		final int quantity = 2;
		lineRequest.setQuantity(quantity);
		createRequest.setProducts(List.of(lineRequest));

		final ResultActions resultActions = mockMvc.perform(post(ORDERS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(createRequest)));

		resultActions
			.andExpect(status().isNotFound());
	}

	private void confirm(String orderLocation) throws Exception {
		updateStatus(orderLocation, "/confirm") //$NON-NLS-1$
			.andExpect(status().isOk())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(Status.CONFIRMED.name()));
	}

	private void cancel(String orderLocation) throws Exception {
		updateStatus(orderLocation, "/cancel") //$NON-NLS-1$
			.andExpect(status().isOk())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(Status.CANCELLED.name()));
	}

	private void cannotShip(String orderLocation) throws Exception {
		updateStatus(orderLocation, "/ship").andExpect(status().isForbidden()); //$NON-NLS-1$
	}

	private ResultActions updateStatus(String orderLocation, String endpoint) throws Exception {
		return mockMvc.perform(post(orderLocation + endpoint)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer()));
	}

	private ResultActions doCreateOrder(List<Pair<ProductResponse, Integer>> lines) throws Exception {
		final CreateOrderRequest createRequest = new CreateOrderRequest();
		for (final Pair<ProductResponse, Integer> line : lines) {
			final OrderLineRequest lineRequest = new OrderLineRequest();
			lineRequest.setProductId(UUID.fromString(line.getFirst().getExternalId()));
			lineRequest.setQuantity(line.getSecond());
			createRequest.getProducts().add(lineRequest);
		}

		return mockMvc.perform(post(ORDERS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(createRequest)));
	}

	// helper method that retrieves the first products page
	private List<ProductResponse> getProducts() throws Exception {
		final MvcResult result = mockMvc.perform(get(PRODUCTS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer())
				.param("size", String.valueOf(ProductIT.PAGE_SIZE))) //$NON-NLS-1$
			.andExpect(status().isOk())
			.andReturn();
		final String content = result.getResponse().getContentAsString();
		final JsonNode root = objectMapper.readTree(content);
		final JsonNode contentNode = root.get(Constants.PAGE_CONTENT_ATTR);

		return objectMapper.readValue(
		        contentNode.toString(),
		        new TypeReference<List<ProductResponse>>() {}
		);
	}

}
