package com.orderprocessing.orders.services;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderprocessing.common.constants.Constants;
import com.orderprocessing.common.security.SecurityUtils;
import com.orderprocessing.orders.dto.CreateProductRequest;
import com.orderprocessing.orders.dto.ProductResponse;
import com.orderprocessing.orders.dto.UpdateProductRequest;
import com.orderprocessing.orders.entities.Product;
import com.orderprocessing.orders.exceptions.ProductNotFoundException;
import com.orderprocessing.orders.mappers.ProductMapper;
import com.orderprocessing.orders.props.OrderProps;
import com.orderprocessing.orders.repositories.ProductRepository;

@Service
public class ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductService.class);

	private final ProductRepository repository;

	private final ProductMapper mapper;

	private final OrderProps orderProps;

	public ProductService(ProductRepository repository, ProductMapper mapper, OrderProps orderProps) {
		this.repository = repository;
		this.mapper = mapper;
		this.orderProps = orderProps;
	}


	/**
	 * Retrieves all the products from the database.
	 * If the user has admin role, the inactive products are included as well.
	 * @return A collection of {@link ProductResponse} representing the products.
	 */
	@Transactional(readOnly = true)
	public Page<ProductResponse> getProducts(Pageable pageable) {

		final boolean filterInactive = ! SecurityUtils.hasRole(Constants.ADMIN_ROLE);

		final Pageable pageRequest = getPagingRequest(pageable);

		log.debug("Finding {} products", filterInactive ? "active" : "all");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		if (filterInactive) {
			return repository.findByActiveTrue(pageRequest)
					.map(mapper::toResponse);
		}
		return repository.findAll(pageRequest)
			.map(mapper::toResponse);
	}

	/**
	 * Creates a new product. Requires admin role.
	 * @param createRequest The new product {@link CreateProductRequest} details.
	 * @return A {@link ProductResponse} representing the new product state.
	 */
	@Transactional
	public ProductResponse createProduct(CreateProductRequest createRequest) {
		SecurityUtils.confirmAdminRole();

		final Product newProduct = mapper.toEntity(createRequest);
		newProduct.setActive(true);
		newProduct.setExternalId(UUID.randomUUID());
		final Product saved = repository.save(newProduct);
		return mapper.toResponse(saved);
	}

	/**
	 * Updates an existing product. Requires admin role.
	 * @param externalId Product unique identifier.
	 * @param updateRequest The product {@link UpdateProductRequest} changes.
	 * @return A {@link ProductResponse} representing the product updated state.
	 */
	@Transactional
	public ProductResponse updateProduct(UUID externalId, UpdateProductRequest updateRequest) {
		SecurityUtils.confirmAdminRole();

		log.debug("Finding product identified by {}", externalId); //$NON-NLS-1$
		final Product product = repository.findByExternalId(externalId)
				.orElseThrow(ProductNotFoundException::new);
		// reminder: SKU never changes; create a new product for a different SKU
		if (null != updateRequest.getName()) {
			product.setName(updateRequest.getName());
		}
		if (null != updateRequest.getDescription()) {
			product.setDescription(updateRequest.getDescription());
		}
		if (null != updateRequest.getCost()) {
			product.setCost(mapper.toMoney(updateRequest.getCost()));
		}
		return mapper.toResponse(product);
	}

	/**
	 * Archives an existing product. Requires admin role.
	 * @param externalId Product unique identifier.
	 * @return A {@link ProductResponse} representing the product updated state.
	 */
	@Transactional
	public ProductResponse deleteProduct(UUID externalId) {
		SecurityUtils.confirmAdminRole();

		log.debug("Finding product identified by {}", externalId); //$NON-NLS-1$
		final Product product = repository.findByExternalId(externalId)
				.orElseThrow(ProductNotFoundException::new);
		product.setActive(false);
		return mapper.toResponse(product);
	}

	private Pageable getPagingRequest(Pageable pageable) {
		int pageNo = 0;
		int pageSize = orderProps.getPageSize();
		Sort sortBy = null == orderProps.getDefaultSortAttribute() ?
				null : Sort.by(orderProps.getDefaultSortAttribute());
		if (null != pageable) {
			if (pageable.getPageNumber() > 0) {
				pageNo = pageable.getPageNumber();
			}
			final int requestSize = pageable.getPageSize();
			if (requestSize > 0
			&& requestSize <= Constants.PAGE_SIZE_HARD_LIMIT // system imposed limit
			&& requestSize <= orderProps.getMaxPageSize()) {
				pageSize = requestSize;
			}
			if (null == sortBy) {
				sortBy = pageable.getSort();
			} else {
				sortBy = pageable.getSortOr(sortBy);
			}
		}
		if (null == sortBy) {
			return PageRequest.of(pageNo, pageSize);
		}
		return PageRequest.of(pageNo, pageSize, sortBy);
	}
}
