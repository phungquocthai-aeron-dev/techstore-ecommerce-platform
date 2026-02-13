package com.techstore.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.techstore.product.entity.Variant;

public interface VariantRepository extends JpaRepository<Variant, Long> {

    List<Variant> findByProductId(Long productId);

    List<Variant> findByIdIn(List<Long> ids);

    boolean existsByProductIdAndColor(Long productId, String color);

    @Query(
            """
			SELECT MIN(v.price)
			FROM Variant v
			WHERE v.product.id = :productId
			AND v.status = 'ACTIVE'
		""")
    Double findMinActivePriceByProductId(Long productId);

    boolean existsByProductIdAndColorAndIdNot(Long productId, String color, Long id);
}
