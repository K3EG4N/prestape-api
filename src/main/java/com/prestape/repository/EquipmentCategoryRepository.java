package com.prestape.repository;

import com.prestape.entity.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EquipmentCategoryRepository
        extends JpaRepository<EquipmentCategory, Long>, JpaSpecificationExecutor<EquipmentCategory> {

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
