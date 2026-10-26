package com.prestape.repository;

import java.util.List;

import com.prestape.entity.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface EquipmentCategoryRepository
        extends JpaRepository<EquipmentCategory, Long>, JpaSpecificationExecutor<EquipmentCategory> {

    interface CategoryStock {
        Long getId();

        String getName();

        Long getTotal();

        Long getAvailable();
    }

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
            select c.id as id, c.name as name, count(e) as total,
                   coalesce(sum(case when e.availability = com.prestape.entity.enums.EquipmentAvailability.AVAILABLE
                                     and e.condition <> com.prestape.entity.enums.EquipmentCondition.DAMAGED
                                     then 1 else 0 end), 0) as available
            from EquipmentCategory c
            left join Equipment e on e.category = c and e.deletedAt is null
            group by c.id, c.name
            order by count(e) desc, c.name
            """)
    List<CategoryStock> findStockByCategory();
}
