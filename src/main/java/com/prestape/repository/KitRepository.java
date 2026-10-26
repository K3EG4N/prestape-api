package com.prestape.repository;

import java.util.Collection;
import java.util.List;

import com.prestape.entity.Kit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KitRepository extends JpaRepository<Kit, Long>, JpaSpecificationExecutor<Kit> {

    interface AssignedEquipment {
        String getEquipmentName();

        String getKitName();
    }

    boolean existsByCode(String code);

    @Query("""
            select e.name as equipmentName, k.name as kitName
            from Kit k join k.equipments e
            where e.id in :equipmentIds and k.id <> :kitId
            """)
    List<AssignedEquipment> findAssignedToOtherKits(
            @Param("equipmentIds") Collection<Long> equipmentIds, @Param("kitId") Long kitId);
}
