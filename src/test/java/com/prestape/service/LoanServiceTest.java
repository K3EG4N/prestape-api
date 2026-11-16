package com.prestape.service;

import static com.prestape.support.TestFixtures.ADMIN;
import static com.prestape.support.TestFixtures.LIMA;
import static com.prestape.support.TestFixtures.STUDENT;
import static com.prestape.support.TestFixtures.availableEquipment;
import static com.prestape.support.TestFixtures.defaultRules;
import static com.prestape.support.TestFixtures.equipment;
import static com.prestape.support.TestFixtures.kit;
import static com.prestape.support.TestFixtures.loan;
import static com.prestape.support.TestFixtures.principal;
import static com.prestape.support.TestFixtures.role;
import static com.prestape.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.prestape.config.ReportProperties;
import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.loan.LoanLimit;
import com.prestape.dto.request.loan.create.CreateLoanRequest;
import com.prestape.dto.request.loan.item.LoanItemConditionRequest;
import com.prestape.dto.request.loan.pickup.RegisterPickupRequest;
import com.prestape.dto.request.loan.returns.RegisterReturnRequest;
import com.prestape.entity.Equipment;
import com.prestape.entity.Kit;
import com.prestape.entity.Loan;
import com.prestape.entity.User;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;
import com.prestape.entity.enums.LoanStatus;
import com.prestape.exception.custom.BusinessException;
import com.prestape.exception.custom.ResourceNotFoundException;
import com.prestape.repository.EquipmentRepository;
import com.prestape.repository.KitRepository;
import com.prestape.repository.LoanRepository;
import com.prestape.repository.PersonRepository;
import com.prestape.repository.UserRepository;
import com.prestape.support.TestFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("Préstamos")
class LoanServiceTest {

    private static final Long ADMIN_ID = 1L;
    private static final Long STUDENT_ID = 10L;
    private static final Long OTHER_STUDENT_ID = 11L;

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private KitRepository kitRepository;
    @Mock
    private SettingsService settingsService;

    private LoanService loanService;
    private User admin;
    private User student;
    private final CustomUserDetails adminPrincipal = principal(ADMIN_ID, ADMIN);
    private final CustomUserDetails studentPrincipal = principal(STUDENT_ID, STUDENT);

    @BeforeEach
    void setUp() {
        loanService = new LoanService(loanRepository, userRepository, personRepository, equipmentRepository,
                kitRepository, new ReportProperties(LIMA, 100), settingsService);
        admin = user(ADMIN_ID, role(1L, ADMIN));
        student = user(STUDENT_ID, role(3L, STUDENT));

        lenient().when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
        lenient().when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        lenient().when(settingsService.loanRules()).thenReturn(defaultRules());
        lenient().when(settingsService.tolerance()).thenReturn(Duration.ofMinutes(45));
        lenient().when(settingsService.limitFor(admin)).thenReturn(Optional.empty());
        lenient().when(settingsService.limitFor(student)).thenReturn(Optional.of(new LoanLimit(2, 1)));
        lenient().when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static Instant hoursFromNow(long hours) {
        return Instant.now().plus(Duration.ofHours(hours));
    }

    private static CreateLoanRequest request(Long requesterId, List<Long> equipmentIds, Long kitId,
                                             Instant pickupAt, Instant returnAt) {
        return new CreateLoanRequest(requesterId, equipmentIds, kitId, pickupAt, returnAt, "  Para clase  ");
    }

    private static CreateLoanRequest equipmentRequest(List<Long> equipmentIds) {
        return request(null, equipmentIds, null, hoursFromNow(3), hoursFromNow(24));
    }

    private Loan captureSavedLoan() {
        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(captor.capture());
        return captor.getValue();
    }

    @Nested
    @DisplayName("Registro de préstamo")
    class Create {

        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', textBlock = """
                CP-01 · Válido: 1 equipo                    | 1 |
                CP-02 · Valor límite: 2 equipos (máximo)    | 2 |
                CP-03 · Valor límite: 3 equipos (excede)    | 3 | El solicitante puede llevar como máximo 2 equipos sueltos; usa un kit para llevar más
                CP-04 · Inválido: sin equipos ni kit        | 0 | Debe seleccionar al menos un equipo o un kit
                """)
        @DisplayName("RN-03 · Un estudiante lleva como máximo 2 equipos sueltos")
        void looseEquipmentLimit(String caseId, int quantity, String expectedError) {
            List<Long> ids = LongStream.rangeClosed(1, quantity).boxed().toList();
            List<Equipment> equipments = ids.stream().map(TestFixtures::availableEquipment).toList();
            lenient().when(equipmentRepository.findAllById(any())).thenReturn(equipments);
            CreateLoanRequest request = equipmentRequest(ids);

            if (expectedError != null) {
                assertThatThrownBy(() -> loanService.create(request, studentPrincipal))
                        .isInstanceOf(BusinessException.class)
                        .hasMessage(expectedError);
                return;
            }

            loanService.create(request, studentPrincipal);

            Loan saved = captureSavedLoan();
            assertThat(saved.getCode()).startsWith("PR-");
            assertThat(saved.getStatus()).isEqualTo(LoanStatus.PENDING_PICKUP);
            assertThat(saved.getRequester()).isEqualTo(student);
            assertThat(saved.getItems()).hasSize(quantity);
            assertThat(equipments)
                    .allSatisfy(equipment -> assertThat(equipment.getAvailability()).isEqualTo(EquipmentAvailability.LOANED));
        }

        @Test
        @DisplayName("Los equipos del kit no cuentan para el máximo de equipos sueltos")
        void kitSkipsEquipmentLimit() {
            Equipment loose = availableEquipment(1L);
            Kit kit = kit(5L, availableEquipment(2L), availableEquipment(3L), availableEquipment(4L));
            when(kitRepository.findById(5L)).thenReturn(Optional.of(kit));
            when(equipmentRepository.findAllById(any())).thenReturn(List.of(loose));

            CreateLoanRequest request = request(null, List.of(1L), 5L, hoursFromNow(3), hoursFromNow(24));
            loanService.create(request, studentPrincipal);

            Loan saved = captureSavedLoan();
            assertThat(saved.getKit()).isEqualTo(kit);
            assertThat(saved.getItems()).hasSize(4);
        }

        @Test
        @DisplayName("Exige la anticipación mínima a los estudiantes")
        void requiresMinimumAdvance() {
            Instant pickupAt = Instant.now().plus(Duration.ofMinutes(30));
            CreateLoanRequest request = request(null, List.of(1L), null, pickupAt, hoursFromNow(24));

            assertThatThrownBy(() -> loanService.create(request, studentPrincipal))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("El recojo debe programarse con al menos 1 h de anticipación");
        }
    }

    @Nested
    @DisplayName("Cancelación")
    class Cancel {

        @Test
        @DisplayName("Un estudiante no puede cancelar el préstamo de otro")
        void studentCannotCancelOthersLoan() {
            Loan loan = loan(100L, user(OTHER_STUDENT_ID), LoanStatus.PENDING_PICKUP);
            when(loanRepository.findById(100L)).thenReturn(Optional.of(loan));

            assertThatThrownBy(() -> loanService.cancel(100L, studentPrincipal))
                    .isInstanceOf(ResourceNotFoundException.class);
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.PENDING_PICKUP);
        }
    }

    @Nested
    @DisplayName("Registro de recojo")
    class Pickup {

        @Test
        @DisplayName("No permite entregar un equipo dañado")
        void rejectsDamagedPickup() {
            Loan loan = loan(100L, student, LoanStatus.PENDING_PICKUP, availableEquipment(1L));
            when(loanRepository.findById(100L)).thenReturn(Optional.of(loan));

            RegisterPickupRequest request = new RegisterPickupRequest(
                    List.of(new LoanItemConditionRequest(1L, EquipmentCondition.DAMAGED)));

            assertThatThrownBy(() -> loanService.registerPickup(100L, request, adminPrincipal))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageStartingWith("No se puede entregar un equipo dañado");
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.PENDING_PICKUP);
        }
    }

    @Nested
    @DisplayName("Registro de devolución")
    class Return {

        private Loan activeLoan(Instant scheduledReturnAt, Equipment... equipments) {
            Loan loan = loan(100L, student, LoanStatus.ACTIVE, equipments);
            loan.setScheduledReturnAt(scheduledReturnAt);
            loan.getItems().forEach(item -> item.setPickupCondition(EquipmentCondition.GOOD));
            when(loanRepository.findById(100L)).thenReturn(Optional.of(loan));
            return loan;
        }

        @Test
        @DisplayName("Una devolución después de la tolerancia queda como tardía")
        void lateReturn() {
            Loan loan = activeLoan(Instant.now().minus(Duration.ofHours(2)),
                    equipment(1L, EquipmentCondition.GOOD, EquipmentAvailability.LOANED));

            loanService.registerReturn(100L, new RegisterReturnRequest(List.of()), adminPrincipal);

            assertThat(loan.isReturnedLate()).isTrue();
        }
    }

    @Nested
    @DisplayName("Cancelación automática")
    class AutoCancel {

        @Test
        @DisplayName("Cancela los préstamos no recogidos dentro del plazo y libera sus equipos")
        void cancelsExpiredPickups() {
            Equipment loaned = equipment(1L, EquipmentCondition.GOOD, EquipmentAvailability.LOANED);
            Loan first = loan(100L, student, LoanStatus.PENDING_PICKUP, loaned);
            Loan second = loan(101L, student, LoanStatus.PENDING_PICKUP);
            when(loanRepository.findAllByStatusAndScheduledPickupAtBefore(eq(LoanStatus.PENDING_PICKUP), any()))
                    .thenReturn(List.of(first, second));

            Instant before = Instant.now();
            int cancelled = loanService.cancelExpiredPickups();

            assertThat(cancelled).isEqualTo(2);
            assertThat(List.of(first, second))
                    .allSatisfy(loan -> assertThat(loan.getStatus()).isEqualTo(LoanStatus.CANCELLED));
            assertThat(loaned.getAvailability()).isEqualTo(EquipmentAvailability.AVAILABLE);

            ArgumentCaptor<Instant> threshold = ArgumentCaptor.forClass(Instant.class);
            verify(loanRepository).findAllByStatusAndScheduledPickupAtBefore(eq(LoanStatus.PENDING_PICKUP), threshold.capture());
            assertThat(threshold.getValue()).isBetween(before.minus(Duration.ofHours(2)), Instant.now().minus(Duration.ofHours(2)));
        }
    }
}
