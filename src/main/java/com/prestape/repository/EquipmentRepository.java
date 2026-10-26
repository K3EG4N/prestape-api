package com.prestape.repository;

import java.util.Collection;
import java.util.List;

import com.prestape.entity.Equipment;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("""
            select e from Equipment e
            where not exists (
                select 1 from Kit k join k.equipments ke
                where ke.id = e.id and k.id <> :kitId)
            """)
    List<Equipment> findAssignableToKit(@Param("kitId") Long kitId, Sort sort);

    @Modifying
    @Query(value = "DELETE FROM kit_items WHERE equipment_id = :equipmentId", nativeQuery = true)
    void removeFromKits(@Param("equipmentId") Long equipmentId);

    interface CategoryEquipmentName {
        Long getCategoryId();

        String getName();
    }

    @Query("select e.category.id as categoryId, e.name as name from Equipment e order by e.name")
    List<CategoryEquipmentName> findNamesByCategory();

    interface AvailabilityCount {
        EquipmentAvailability getStatus();

        Long getTotal();
    }

    interface ConditionCount {
        EquipmentCondition getStatus();

        Long getTotal();
    }

    @Query("select e.availability as status, count(e) as total from Equipment e group by e.availability")
    List<AvailabilityCount> countByAvailability();

    @Query("select e.condition as status, count(e) as total from Equipment e group by e.condition")
    List<ConditionCount> countByCondition();

    @EntityGraph(attributePaths = "category")
    List<Equipment> findTop5ByAvailabilityOrConditionOrderByUpdatedAtDesc(
            EquipmentAvailability availability, EquipmentCondition condition);
}
