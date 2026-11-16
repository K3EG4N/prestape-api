package com.prestape.entity;

import static com.prestape.support.TestFixtures.equipment;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;

@DisplayName("Equipo")
class EquipmentTest {

    @Test
    @DisplayName("Al liberarse de un préstamo dañado pasa a mantenimiento")
    void releaseDamagedEquipment() {
        Equipment equipment = equipment(1L, EquipmentCondition.DAMAGED, EquipmentAvailability.LOANED);

        equipment.releaseFromLoan();

        assertThat(equipment.getAvailability()).isEqualTo(EquipmentAvailability.IN_MAINTENANCE);
    }
}
