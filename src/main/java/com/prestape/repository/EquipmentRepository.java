package com.prestape.repository;

import java.util.Collection;
import java.util.List;

import com.prestape.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {

    interface CategoryCount {
        Long getCategoryId();

        Long getTotal();
    }

    @Query("""
            select e.category.id as categoryId, count(e) as total
            from Equipment e
            where e.category.id in :categoryIds
            group by e.category.id
            """)
    List<CategoryCount> countByCategoryIds(@Param("categoryIds") Collection<Long> categoryIds);

    long countByCategoryId(Long categoryId);

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Equipment> findAll(Specification<Equipment> spec, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsBySerialNumberIgnoreCase(String serialNumber);

    boolean existsBySerialNumberIgnoreCaseAndIdNot(String serialNumber, Long id);
}
