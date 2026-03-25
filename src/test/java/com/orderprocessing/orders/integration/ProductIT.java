package com.orderprocessing.orders.integration;

import static com.orderprocessing.common.tests.TestConstants.ADMIN;
import static com.orderprocessing.orders.constants.Constants.PRODUCTS_PATH;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Currency;
import java.util.Random;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.orderprocessing.common.constants.Constants;
import com.orderprocessing.common.tests.AbstractIntegrationTestBase;
import com.orderprocessing.order_processing_orders.OrderProcessingOrdersApplication;
import com.orderprocessing.orders.dto.CreateProductRequest;
import com.orderprocessing.orders.dto.MoneyDTO;
import com.orderprocessing.orders.dto.ProductResponse;
import com.orderprocessing.orders.dto.UpdateProductRequest;

@SpringBootTest(classes = OrderProcessingOrdersApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles(value={"test"})
@Transactional
class ProductIT extends AbstractIntegrationTestBase{

	private static final Logger log = LoggerFactory.getLogger(ProductIT.class);

	static final int PAGE_SIZE = 50; // expected predefined products

	private static final String USD = "USD"; //$NON-NLS-1$
	private static final String FIELD_AMOUNT = "amount"; //$NON-NLS-1$
	private static final String FIELD_SKU = "sku"; //$NON-NLS-1$
	private static final String FIELD_NAME = "name"; //$NON-NLS-1$
	private static final String FIELD_DESC = "description"; //$NON-NLS-1$
	private static final Object FIELD_CURRENCY = "currency"; //$NON-NLS-1$
	private static final String FIELD_EXTERNAL_ID = "externalId"; //$NON-NLS-1$
	private static final String CNT_ARRAY_MEMBR_TMPLT = "$.%s[%d].%s"; //$NON-NLS-1$
	private static final String CNT_ARRAY_MEMBR_COST_TMPLT = "$.%s[%d].cost.%s"; //$NON-NLS-1$
	private static final String MEMBR_COST_TMPLT = "$.cost.%s"; //$NON-NLS-1$
	private static final String MEMBR_TMPLT = "$.%s"; //$NON-NLS-1$
	private static final String ISO_4217_REGEX = "^[A-Z]{3}$"; //$NON-NLS-1$

	@Value("${jwt.secret}")
	private String secret;

	private final UUID userExternalId = UUID.randomUUID();

	@Override
	protected Logger getLog() {
		return log;
	}

	@BeforeEach
	void login() {
		mockLogin(ADMIN, Constants.ADMIN_UUID0, Constants.ADMIN_ROLE, secret);
	}

	@Test
	@DisplayName("Tests retrieving existing products")
	void testGetAllProducts() throws Exception {
		doGetAllProducts();
	}

	@Test
	@DisplayName("Tests retrieving existing products as a regular user")
	@Rollback
	void testGetAllProductsAsRegularUser() throws Exception {
		mockLogin(TEST_USER, userExternalId, Constants.USER_ROLE, secret);

		doGetAllProducts();
	}

	@Test
	@DisplayName("Tests creating a product")
	@Rollback
	void testCreateProduct() throws Exception {
		doCreateProduct(true);
	}

	@Test
	@DisplayName("Negative: Tests creating a product as a regular user")
	@Rollback
	void testCreateProductAsRegularUser() throws Exception {
		mockLogin(TEST_USER, userExternalId, Constants.USER_ROLE, secret);

		doCreateProduct(false);
	}

	@Test
	@DisplayName("Tests updating a product")
	@Rollback
	void testUpdateProduct() throws Exception {
		final ProductResponse newProduct = doCreateProduct(true);

		doUpdateProduct(newProduct, true);
	}

	@Test
	@DisplayName("Tests updating a product as a regular user")
	@Rollback
	void testUpdateProductAsRegularUser() throws Exception {
		final ProductResponse newProduct = doCreateProduct(true);

		mockLogin(TEST_USER, userExternalId, Constants.USER_ROLE, secret);

		doUpdateProduct(newProduct, false);
	}

	@Test
	@DisplayName("Tests deleting a product")
	@Rollback
	void testDeleteProduct() throws Exception {
		final ProductResponse newProduct = doCreateProduct(true);

		doDeleteProduct(newProduct, true);
	}

	@Test
	@DisplayName("Tests deleting a product as a regular user")
	@Rollback
	void testDeleteProductAsRegularUser() throws Exception {
		final ProductResponse newProduct = doCreateProduct(true);

		mockLogin(TEST_USER, userExternalId, Constants.USER_ROLE, secret);

		doDeleteProduct(newProduct, false);
	}

	private void doDeleteProduct(
			ProductResponse newProduct,
			boolean expectSuccessful) throws Exception {
		final ResultActions resultActions = mockMvc
				.perform(delete(PRODUCTS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, getBearer())
						.param(Constants.PARAM_EXTERNAL_ID, newProduct.getExternalId()));
		if (expectSuccessful) {
			resultActions.andExpect(status().isNoContent());

		} else {
			resultActions.andExpect(status().isForbidden());
		}
	}

	private ProductResponse doUpdateProduct(
			ProductResponse newProduct,
			boolean expectSuccessful) throws Exception {
		final String changedName = "Modified test product"; //$NON-NLS-1$
		final MoneyDTO changedCostDTO = new MoneyDTO(BigDecimal.valueOf(177D), Currency.getInstance(USD));
		final UpdateProductRequest updateRequest = new UpdateProductRequest();
		updateRequest.setName(changedName);
		updateRequest.setCost(changedCostDTO);

		final ResultActions resultActions = mockMvc
				.perform(patch(PRODUCTS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, getBearer())
						.param(Constants.PARAM_EXTERNAL_ID, newProduct.getExternalId())
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.characterEncoding(StandardCharsets.UTF_8)
						.content(objectMapper.writeValueAsString(updateRequest)));
		if (expectSuccessful) {
			resultActions
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID).isNotEmpty())
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID).value(newProduct.getExternalId()))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_NAME).value(changedName))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_SKU).value(newProduct.getSku()))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_DESC).value(newProduct.getDescription()))
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_AMOUNT).isNumber())
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_AMOUNT).value(177D))
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_CURRENCY).isNotEmpty())
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_CURRENCY).value(USD));
			final MvcResult result = resultActions.andReturn();
			final String content = result.getResponse().getContentAsString();
			return objectMapper.readValue(content, ProductResponse.class);

		}
		resultActions
				.andExpect(status().isForbidden());
		return null;
	}

	private ProductResponse doCreateProduct(boolean expectSuccessful) throws Exception {
		final var createRequest = new CreateProductRequest();
		final String expectedSku = "SKU-0000246"; //$NON-NLS-1$
		createRequest.setSku(expectedSku);
		final String expectedName = "Test product"; //$NON-NLS-1$
		createRequest.setName(expectedName);
		final String expectedDesc = "Dummy product"; //$NON-NLS-1$
		createRequest.setDescription(expectedDesc);
		final MoneyDTO moneyDTO = new MoneyDTO(BigDecimal.valueOf(123.45D), Currency.getInstance(USD));
		createRequest.setCost(moneyDTO);

		final ResultActions resultActions = mockMvc
				.perform(post(PRODUCTS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, getBearer())
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.characterEncoding(StandardCharsets.UTF_8)
						.content(objectMapper.writeValueAsString(createRequest)));
		if (expectSuccessful) {
			resultActions
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID).isNotEmpty())
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID).value(matchesPattern(UUID_REGEX)))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_NAME).value(expectedName))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_SKU).value(expectedSku))
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_AMOUNT).isNumber())
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_AMOUNT).value(123.45D))
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_CURRENCY).isNotEmpty())
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_CURRENCY).value(USD));
			final MvcResult result = resultActions.andReturn();
			final String content = result.getResponse().getContentAsString();
			return objectMapper.readValue(content, ProductResponse.class);

		}
		resultActions
				.andExpect(status().isForbidden());
		return null;
	}

	private void doGetAllProducts() throws Exception {
		final int randomSelection = new Random().nextInt(PAGE_SIZE);
		final MvcResult result = mockMvc
				.perform(get(PRODUCTS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, getBearer())
						.param("size", String.valueOf(PAGE_SIZE))) //$NON-NLS-1$
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR).isArray()) //$NON-NLS-1$
				.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR + ".length()").value(PAGE_SIZE)) //$NON-NLS-1$ //$NON-NLS-2$
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, Constants.PAGE_CONTENT_ATTR,
						randomSelection, FIELD_EXTERNAL_ID).isNotEmpty())
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, Constants.PAGE_CONTENT_ATTR,
						randomSelection, FIELD_EXTERNAL_ID).value(matchesPattern(UUID_REGEX)))
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, Constants.PAGE_CONTENT_ATTR,
						randomSelection, FIELD_NAME).isNotEmpty())
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, Constants.PAGE_CONTENT_ATTR,
						randomSelection, FIELD_SKU).isNotEmpty())
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, Constants.PAGE_CONTENT_ATTR,
						randomSelection, FIELD_AMOUNT).isNumber())
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, Constants.PAGE_CONTENT_ATTR,
						randomSelection, FIELD_AMOUNT).value(Matchers.greaterThanOrEqualTo(0.0D)))
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, Constants.PAGE_CONTENT_ATTR,
						randomSelection, FIELD_CURRENCY).isNotEmpty())
				.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, Constants.PAGE_CONTENT_ATTR,
						randomSelection, FIELD_CURRENCY).value(matchesPattern(ISO_4217_REGEX)))
				.andReturn();
		if (log.isDebugEnabled()) {
			log.debug(result.getResponse().getContentAsString());
		}
	}

}
