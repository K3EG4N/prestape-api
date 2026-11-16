package com.prestape.support;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.loan.LoanRules;
import com.prestape.entity.BusinessHour;
import com.prestape.entity.Equipment;
import com.prestape.entity.Kit;
import com.prestape.entity.Loan;
import com.prestape.entity.LoanItem;
import com.prestape.entity.Role;
import com.prestape.entity.User;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;
import com.prestape.entity.enums.LoanStatus;

public final class TestFixtures {

    public static final ZoneId LIMA = ZoneId.of("America/Lima");
    public static final String STUDENT = "STUDENT";
    public static final String TEACHER = "TEACHER";
    public static final String ADMIN = "ADMIN";
    public static final String SUPERVISOR = "SUPERVISOR";

    private TestFixtures() {
    }

    public static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    public static Role role(Long id, String name) {
        Role role = withId(new Role(name), id);
        role.setExternalName(name);
        return role;
    }

    public static User user(Long id, Role... roles) {
        User user = withId(new User("user" + id, "user" + id + "@prestape.pe", "secret"), id);
        user.markProfileCompleted();
        user.setRoles(Set.of(roles));
        return user;
    }

    public static CustomUserDetails principal(Long id, String... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return new CustomUserDetails(id, "user" + id + "@prestape.pe", "secret", authorities, false, true);
    }

    public static Equipment equipment(Long id, EquipmentCondition condition, EquipmentAvailability availability) {
        Equipment equipment = withId(new Equipment(), id);
        equipment.setCode("EQ-" + id);
        equipment.setName("Equipo " + id);
        equipment.setCondition(condition);
        equipment.setAvailability(availability);
        return equipment;
    }

    public static Equipment availableEquipment(Long id) {
        return equipment(id, EquipmentCondition.GOOD, EquipmentAvailability.AVAILABLE);
    }

    public static Kit kit(Long id, Equipment... equipments) {
        Kit kit = withId(new Kit(), id);
        kit.setCode("KT-" + id);
        kit.setName("Kit " + id);
        kit.setEquipments(Arrays.stream(equipments).collect(Collectors.toSet()));
        return kit;
    }

    public static Loan loan(Long id, User requester, LoanStatus status, Equipment... equipments) {
        Loan loan = withId(new Loan(), id);
        loan.setCode("PR-" + id);
        loan.setRequester(requester);
        loan.setStatus(status);
        loan.setScheduledPickupAt(Instant.now().plus(Duration.ofHours(1)));
        loan.setScheduledReturnAt(Instant.now().plus(Duration.ofDays(1)));
        for (Equipment equipment : equipments) {
            LoanItem item = new LoanItem();
            item.setEquipment(equipment);
            loan.addItem(item);
        }
        return loan;
    }

    public static BusinessHour businessDay(int day, boolean open, LocalTime opensAt, LocalTime closesAt) {
        BusinessHour hour = new BusinessHour();
        hour.setDayOfWeek((short) day);
        hour.setOpen(open);
        hour.setOpensAt(opensAt);
        hour.setClosesAt(closesAt);
        return hour;
    }

    public static List<BusinessHour> alwaysOpen() {
        return IntStream.rangeClosed(1, 7)
                .mapToObj(day -> businessDay(day, true, LocalTime.MIN, LocalTime.MAX))
                .toList();
    }

    public static LoanRules rules(Duration tolerance, Duration maxDuration, Duration minAdvance,
                                  Duration autoCancel, List<BusinessHour> businessHours) {
        return new LoanRules(tolerance, maxDuration, minAdvance, autoCancel, businessHours, LIMA);
    }

    public static LoanRules defaultRules() {
        return rules(Duration.ofMinutes(45), Duration.ofDays(3), Duration.ofHours(1), Duration.ofHours(2), alwaysOpen());
    }
}
