package com.orderprocessing.orders.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.orderprocessing.orders.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findByExternalId(UUID externalId);

	List<Product> findBySku(String sku);

	Page<Product> findByActiveTrue(Pageable pageable);

	List<Product> findAllByExternalIdIn(Collection<UUID> externalIds);

}
