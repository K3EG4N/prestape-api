package com.prestape.service;

import static com.prestape.support.TestFixtures.ADMIN;
import static com.prestape.support.TestFixtures.LIMA;
import static com.prestape.support.TestFixtures.STUDENT;
import static com.prestape.support.TestFixtures.SUPERVISOR;
import static com.prestape.support.TestFixtures.TEACHER;
import static com.prestape.support.TestFixtures.role;
import static com.prestape.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.prestape.config.AppProperties;
import com.prestape.dto.loan.LoanLimit;
import com.prestape.dto.request.settings.BusinessDayRequest;
import com.prestape.dto.request.settings.RoleLimitRequest;
import com.prestape.dto.request.settings.UpdateSettingsRequest;
import com.prestape.entity.Role;
import com.prestape.entity.RoleLoanLimit;
import com.prestape.entity.SystemSettings;
import com.prestape.repository.BusinessHourRepository;
import com.prestape.repository.PersonRepository;
import com.prestape.repository.RoleLoanLimitRepository;
import com.prestape.repository.RoleRepository;
import com.prestape.repository.SystemSettingsRepository;
import com.prestape.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Servicio de configuración")
class SettingsServiceTest {

    @Mock
    private SystemSettingsRepository settingsRepository;
    @Mock
    private RoleLoanLimitRepository roleLoanLimitRepository;
    @Mock
    private BusinessHourRepository businessHourRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PersonRepository personRepository;

    private SettingsService settingsService;
    private SystemSettings settings;
    private final Role admin = role(1L, ADMIN);
    private final Role supervisor = role(2L, SUPERVISOR);
    private final Role student = role(3L, STUDENT);
    private final Role teacher = role(4L, TEACHER);

    @BeforeEach
    void setUp() {
        settingsService = new SettingsService(settingsRepository, roleLoanLimitRepository, businessHourRepository,
                roleRepository, userRepository, personRepository, new AppProperties(LIMA));

        settings = new SystemSettings();
        settings.setId((short) 1);
        settings.setToleranceMinutes(45);
        settings.setMaxDurationDays(3);
        settings.setMinAdvanceHours(1);
        settings.setAutoCancelHours(2);
        settings.setUpdatedAt(Instant.parse("2026-11-01T12:00:00Z"));

        lenient().when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));
        lenient().when(roleRepository.findAll()).thenReturn(List.of(admin, supervisor, student, teacher));
    }

    private static RoleLoanLimit limit(Role role, int maxEquipments, int maxActiveLoans) {
        RoleLoanLimit limit = new RoleLoanLimit();
        limit.setRoleId(role.getId());
        limit.setRole(role);
        limit.setMaxEquipments(maxEquipments);
        limit.setMaxActiveLoans(maxActiveLoans);
        return limit;
    }

    private static List<BusinessDayRequest> week(boolean open) {
        return IntStream.rangeClosed(1, 7)
                .mapToObj(day -> new BusinessDayRequest((short) day, open, LocalTime.of(8, 0), LocalTime.of(20, 0)))
                .toList();
    }

    private static UpdateSettingsRequest request(List<RoleLimitRequest> roleLimits, List<BusinessDayRequest> days) {
        return new UpdateSettingsRequest(30, 5, 2, 4, roleLimits, days);
    }

    @Nested
    @DisplayName("Límites por usuario")
    class LimitFor {

        @Test
        @DisplayName("Con varios roles toma el límite más amplio de cada valor")
        void usesMostPermissiveLimit() {
            when(roleLoanLimitRepository.findAll()).thenReturn(List.of(limit(student, 4, 1), limit(teacher, 2, 3)));

            assertThat(settingsService.limitFor(user(10L, student, teacher))).contains(new LoanLimit(4, 3));
        }
    }
}
