package com.orderprocessing.orders.unit;

import static com.orderprocessing.common.tests.TestConstants.ADMIN;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.orderprocessing.common.tests.AbstractUnitTestBase;
import com.orderprocessing.orders.dto.CreateProductRequest;
import com.orderprocessing.orders.dto.MoneyDTO;
import com.orderprocessing.orders.dto.ProductResponse;
import com.orderprocessing.orders.dto.UpdateProductRequest;
import com.orderprocessing.orders.entities.Money;
import com.orderprocessing.orders.entities.Product;
import com.orderprocessing.orders.mappers.ProductMapper;
import com.orderprocessing.orders.props.OrderProps;
import com.orderprocessing.orders.repositories.ProductRepository;
import com.orderprocessing.orders.services.ProductService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest extends AbstractUnitTestBase {

	private static final String CAD = "CAD"; //$NON-NLS-1$

	@Mock
	private ProductRepository repository;

	@Mock
	private ProductMapper mapper;

	@Mock
	private OrderProps orderProps;


	@InjectMocks
	private ProductService service;

	@Test
	@DisplayName("Retrieves products from the system")
	void testGetAllProducts() {
		final UUID uuid1 = UUID.randomUUID();
		final UUID uuid2 = UUID.randomUUID();
		final UUID uuid3 = UUID.randomUUID();
		final Product first = new Product("SKU-000001", "LG 34\" UltraWide Monitor", //$NON-NLS-1$//$NON-NLS-2$
				Money.of(BigDecimal.valueOf(399), Currency.getInstance(CAD)));
		first.setActive(true);
		first.setId(1L);
		first.setExternalId(uuid1);
		first.setDescription("34-inch curved IPS monitor"); //$NON-NLS-1$

		final Product second = new Product("SKU-000002", "Sony WH-1000XM5 Headphones", //$NON-NLS-1$//$NON-NLS-2$
				Money.of(BigDecimal.valueOf(349.99), Currency.getInstance(CAD)));
		second.setActive(true);
		second.setId(2L);
		second.setExternalId(uuid2);
		second.setDescription("Noise cancelling wireless headphones"); //$NON-NLS-1$

		final Product third = new Product("SKU-000003", "Anker PowerCore 20000", //$NON-NLS-1$//$NON-NLS-2$
				Money.of(BigDecimal.valueOf(49.99), Currency.getInstance(CAD)));
		third.setActive(true);
		third.setId(3L);
		third.setExternalId(uuid3);
		third.setDescription("20000mAh portable power bank"); //$NON-NLS-1$

		final List<Product> expectedProducts = List.of(first, second, third);

		final String attribute = "name"; //$NON-NLS-1$
		final Pageable request = PageRequest.of(1, 100, Sort.by(attribute));

		lenient().when(repository.findAll(any(Pageable.class)))
			.thenReturn(new PageImpl<>(expectedProducts));

		lenient().when(repository.findByActiveTrue(any(Pageable.class)))
			.thenReturn(new PageImpl<>(expectedProducts));

		given(mapper.toResponse(first))
			.willReturn(new ProductResponse(first.getExternalId().toString(),
											first.getSku(),
											first.getName(),
											first.getDescription(),
											true,
											new MoneyDTO(first.getCost().getAmount(), first.getCost().getCurrency())));

		given(mapper.toResponse(second))
			.willReturn(new ProductResponse(second.getExternalId().toString(),
											second.getSku(),
											second.getName(),
											second.getDescription(),
											true,
											new MoneyDTO(second.getCost().getAmount(), second.getCost().getCurrency())));

		given(mapper.toResponse(third))
			.willReturn(new ProductResponse(third.getExternalId().toString(),
											third.getSku(),
											third.getName(),
											third.getDescription(),
											true,
											new MoneyDTO(third.getCost().getAmount(), third.getCost().getCurrency())));

		given(orderProps.getPageSize())
			.willReturn(50);
		given(orderProps.getMaxPageSize())
			.willReturn(50);
		given(orderProps.getDefaultSortAttribute())
			.willReturn(attribute);

		final Page<ProductResponse> prodPage = service.getProducts(request);
		assertNotNull(prodPage);
		assertEquals(3, prodPage.getContent().size());
		for (int index = 0; index < 3; index++) {
			final var expected = expectedProducts.get(index);
			final var actual = prodPage.getContent().get(index);
			assertEquals(expected.getExternalId().toString(), actual.getExternalId());
			assertEquals(expected.getName(), actual.getName());
			assertEquals(expected.getCost().getAmount(), actual.getCost().getAmount());
			assertEquals(expected.getCost().getCurrency(), actual.getCost().getCurrency());
			assertEquals(expected.getSku(), actual.getSku());
		}
	}

	@Test
	@DisplayName("Tests product creation")
	void testCreateProduct() {
		assertDoesNotThrow(this::doCreateProduct);
	}

	@Test
	@DisplayName("Tests that creating a product as non-admin not possible")
	void testCreateProductAsRegularUser() {
		setupUserNoRole(ADMIN, ADMIN);

		assertThrows(Exception.class, this::doCreateProduct);
	}

	@Test
	@DisplayName("Tests product modification")
	void testUpdateProduct() {
		final ProductResponse response = doCreateProduct();
		assertDoesNotThrow(()->doUpdateProduct(response));
	}

	@Test
	@DisplayName("Tests that updating a product as non-admin not possible")
	void testUpdateProductAsRegularUser() {
		final ProductResponse response = doCreateProduct();

		setupUserNoRole(ADMIN, ADMIN);

		assertThrows(Exception.class, ()->doUpdateProduct(response));
	}

	@Test
	@DisplayName("Tests product archival")
	void testDeleteProduct() {
		final ProductResponse response = doCreateProduct();
		assertDoesNotThrow(()->doDeleteProduct(response));
	}

	@Test
	@DisplayName("Tests that deleting a product as non-admin not possible")
	void testDeleteProductAsRegularUser() {
		final ProductResponse response = doCreateProduct();

		setupUserNoRole(ADMIN, ADMIN);

		assertThrows(Exception.class, ()->doDeleteProduct(response));
	}

	private void doDeleteProduct(ProductResponse creationResponse) {
		final UUID externalId = UUID.fromString(creationResponse.getExternalId());
		final Optional<Product> found = mockEntity(creationResponse, true);
		final var expectedResponse = new ProductResponse(externalId.toString(),
														creationResponse.getSku(),
														creationResponse.getName(),
														creationResponse.getDescription(),
														false,
														creationResponse.getCost());

		lenient().when(repository.findByExternalId(externalId))
				.thenReturn(found);
		final Optional<Product> deleted = mockEntity(creationResponse, false);
		lenient().when(repository.save(found.get()))
    			.thenReturn(deleted.get());
		lenient().when(mapper.toResponse(found.get()))
    			.thenReturn(expectedResponse);

		final ProductResponse response = service.deleteProduct(externalId);

		assertEquals(creationResponse.getName(), response.getName());
        assertEquals(creationResponse.getCost().getAmount(), response.getCost().getAmount());
        assertEquals(creationResponse.getCost().getCurrency(), response.getCost().getCurrency());
        assertEquals(creationResponse.getSku(), response.getSku());
        assertEquals(false, response.isActive());
        assertEquals(creationResponse.getDescription(), response.getDescription());
	}

	private void doUpdateProduct(ProductResponse creationResponse) {
		final String newName = "LG UltraWide Monitor (34\")";  //$NON-NLS-1$
		final String desc = creationResponse.getDescription(); // unchanged
		final MoneyDTO newCost = new MoneyDTO(BigDecimal.valueOf(425.98), Currency.getInstance(CAD));
		final UpdateProductRequest updateRequest = new UpdateProductRequest();
		updateRequest.setName(newName);
		updateRequest.setCost(newCost);

		final Product updated = mockEntity(creationResponse, updateRequest);
		final UUID externalId = UUID.fromString(creationResponse.getExternalId());
		final var expectedResponse = new ProductResponse(externalId.toString(),
														creationResponse.getSku(),
														newName,
														desc,
														true,
														newCost);

        // lenient() allows testing as regular user when an exception is thrown early
		final Optional<Product> found = mockEntity(creationResponse, true);
		lenient().when(repository.findByExternalId(externalId))
			.thenReturn(found);
        lenient().when(repository.save(found.get()))
        	.thenReturn(updated);
        lenient().when(mapper.toResponse(found.get()))
        	.thenReturn(expectedResponse);

        final ProductResponse response = service.updateProduct(externalId, updateRequest);

        assertEquals(newName, response.getName());
        assertEquals(newCost.getAmount(), response.getCost().getAmount());
        assertEquals(newCost.getCurrency(), response.getCost().getCurrency());
        assertEquals(creationResponse.getSku(), response.getSku());
        assertEquals(true, response.isActive());
        assertEquals(desc, response.getDescription());

	}

	private ProductResponse doCreateProduct() {
		final String sku = "SKU-000001"; //$NON-NLS-1$
		final String name = "LG 34\" UltraWide Monitor";  //$NON-NLS-1$
		final String desc = "34-inch curved IPS monitor"; //$NON-NLS-1$
		final MoneyDTO cost = new MoneyDTO(BigDecimal.valueOf(399), Currency.getInstance(CAD));
		final CreateProductRequest productRequest = new CreateProductRequest();
		productRequest.setName(name);
		productRequest.setSku(sku);
		productRequest.setDescription(desc);
		productRequest.setCost(cost);

		final UUID staticUUID = UUID.randomUUID();
		final String expectedExternalId = staticUUID.toString();
		final ProductResponse expectedResponse = new ProductResponse(staticUUID.toString(), sku, name, desc, true, cost);
		try (MockedStatic<UUID> mockedUUID = Mockito.mockStatic(UUID.class)) {
            mockedUUID.when(UUID::randomUUID).thenReturn(staticUUID);

            final Product saved = mockEntity(productRequest);

            // lenient() allows testing as regular user when an exception is thrown early
            lenient().when(repository.save(saved))
            	.thenReturn(saved);
            lenient().when(mapper.toResponse(saved))
            	.thenReturn(expectedResponse);

            final ProductResponse response = service.createProduct(productRequest);

            verify(repository).save(any(Product.class));

            assertEquals(productRequest.getName(), response.getName());
            assertEquals(productRequest.getCost().getAmount(), response.getCost().getAmount());
            assertEquals(productRequest.getCost().getCurrency(), response.getCost().getCurrency());
            assertEquals(productRequest.getSku(), response.getSku());
            assertEquals(true, response.isActive());
            assertEquals(productRequest.getDescription(), response.getDescription());
            assertEquals(expectedExternalId, response.getExternalId());

            return response;
		}
	}

	private Product mockEntity(CreateProductRequest request) {
		final Product entity = new Product(request.getSku(), request.getName(), mockCost(request.getCost()));
		if (null != request.getDescription()) {
			entity.setDescription(request.getDescription());
		}
		entity.setActive(true);

		lenient().when(mapper.toEntity(request))
			.thenReturn(entity);

		return entity;
	}

	private Product mockEntity(ProductResponse creationResponse, UpdateProductRequest updateRequest) {
		final Product entity = new Product();
		if (null != updateRequest.getDescription()) {
			entity.setDescription(updateRequest.getDescription());
		}
		if (null != updateRequest.getName()) {
			entity.setName(updateRequest.getName());
		}
		if (null != updateRequest.getCost()) {
			entity.setCost(mockCost(updateRequest.getCost()));
		}
		entity.setExternalId(UUID.fromString(creationResponse.getExternalId()));
		entity.setActive(true);

		lenient().when(mapper.toEntity(updateRequest))
			.thenReturn(entity);

		return entity;
	}

	private Optional<Product> mockEntity(ProductResponse creationResponse, boolean active) {
		final Product entity = new Product(creationResponse.getSku(), creationResponse.getName(), mockCost(creationResponse.getCost()));
		entity.setActive(active);
		entity.setDescription(creationResponse.getDescription());
		entity.setExternalId(UUID.fromString(creationResponse.getExternalId()));
		return Optional.of(entity);
	}

	private Money mockCost(MoneyDTO cost) {
		return Money.of(cost.getAmount(), cost.getCurrency());
	}
}
