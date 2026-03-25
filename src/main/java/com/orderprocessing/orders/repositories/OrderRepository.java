package com.orderprocessing.orders.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.orderprocessing.orders.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("""
		    SELECT o
		    FROM Order o
		    JOIN FETCH o.orderLines l
		    JOIN FETCH l.product
		    WHERE o.externalId = :id
		""")
		Optional<Order> findByExternalIdWithLinesAndProducts(@Param("id") UUID id);

	Page<Order> findAllByCustomerExternalIdOrderByCreatedDesc(UUID ownerExternalId, Pageable pageable);

	Page<Order> findAllByOrderByCreatedDesc(Pageable pageRequest);

	Optional<Order> findByExternalId(UUID externalId);

}
