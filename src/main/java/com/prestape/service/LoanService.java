package com.prestape.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.prestape.config.ReportProperties;
import com.prestape.constants.KitConstants;
import com.prestape.constants.LoanConstants;
import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.common.SelectOption;
import com.prestape.dto.request.loan.create.CreateLoanRequest;
import com.prestape.dto.request.loan.list.LoanFilterRequest;
import com.prestape.dto.request.loan.item.LoanItemConditionRequest;
import com.prestape.dto.request.loan.pickup.RegisterPickupRequest;
import com.prestape.dto.request.loan.returns.RegisterReturnRequest;
import com.prestape.dto.response.LoanEquipmentOption;
import com.prestape.dto.response.LoanKitOption;
import com.prestape.dto.response.LoanListResponse;
import com.prestape.entity.Equipment;
import com.prestape.entity.Kit;
import com.prestape.entity.Loan;
import com.prestape.entity.LoanItem;
import com.prestape.entity.Person;
import com.prestape.entity.User;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;
import com.prestape.entity.enums.KitAvailability;
import com.prestape.entity.enums.LoanStatus;
import com.prestape.exception.custom.BusinessException;
import com.prestape.exception.custom.ConflictException;
import com.prestape.exception.custom.ResourceNotFoundException;
import com.prestape.repository.EquipmentRepository;
import com.prestape.repository.KitRepository;
import com.prestape.repository.LoanRepository;
import com.prestape.repository.PersonRepository;
import com.prestape.repository.UserRepository;
import com.prestape.security.Roles;
import com.prestape.specifications.EquipmentSpecifications;
import com.prestape.specifications.LoanSpecifications;
import com.prestape.util.RandomCodeGenerator;
import com.prestape.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LoanService {
    private static final Logger log = LoggerFactory.getLogger(LoanService.class);
    private static final Set<String> MANAGER_AUTHORITIES = Set.of("ROLE_" + Roles.ADMIN, "ROLE_" + Roles.SUPERVISOR);
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final EquipmentRepository equipmentRepository;
    private final KitRepository kitRepository;
    private final ReportProperties reportProperties;

    public LoanService(
            LoanRepository loanRepository,
            UserRepository userRepository,
            PersonRepository personRepository,
            EquipmentRepository equipmentRepository,
            KitRepository kitRepository,
            ReportProperties reportProperties
    ) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.equipmentRepository = equipmentRepository;
        this.kitRepository = kitRepository;
        this.reportProperties = reportProperties;
    }

    @Transactional(readOnly = true)
    public PageResponse<LoanListResponse> list(
            PaginationRequest pagination, LoanFilterRequest filter, CustomUserDetails currentUser) {
        Specification<Loan> spec = buildSpecification(pagination.search(), filter);
        if (!isManager(currentUser)) {
            spec = spec.and(LoanSpecifications.requestedBy(currentUser.getId()));
        }

        Page<Loan> loans = loanRepository.findAll(spec, pagination.toPageable(LoanConstants.DEFAULT_SORT));
        Map<Long, Person> peopleByUserId = findPeopleByUserId(loans.getContent());

        return PageResponse.from(loans.map(loan -> LoanListResponse.from(loan, peopleByUserId)));
    }

    @Transactional(readOnly = true)
    public List<LoanListResponse> listForExport(String search, LoanFilterRequest filter) {
        Specification<Loan> spec = buildSpecification(search, filter);

        int maxRows = reportProperties.maxRows();
        if (loanRepository.count(spec) > maxRows) {
            throw new BusinessException(
                    "La exportación supera las " + maxRows + " filas. Aplica más filtros");
        }

        List<Loan> loans = loanRepository.findAll(spec, LoanConstants.DEFAULT_SORT);
        Map<Long, Person> peopleByUserId = findPeopleByUserId(loans);

        return loans.stream()
                .map(loan -> LoanListResponse.from(loan, peopleByUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SelectOption> requesterOptions() {
        return personRepository.findLoanRequesters().stream()
                .map(person -> new SelectOption(person.getUser().getId(), person.getFullName(), person.getCode()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoanEquipmentOption> equipmentOptions(String search) {
        Specification<Equipment> spec = EquipmentSpecifications.isLendable();
        if (StringUtils.hasText(search)) {
            spec = spec.and(EquipmentSpecifications.matchesSearch(search.trim()));
        }

        PageRequest page = PageRequest.of(0, LoanConstants.OPTIONS_LIMIT, LoanConstants.OPTIONS_SORT);
        return equipmentRepository.findAll(spec, page).stream()
                .map(LoanEquipmentOption::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoanKitOption> kitOptions() {
        return kitRepository.findAll(KitConstants.DEFAULT_SORT).stream()
                .filter(kit -> kit.getAvailability() == KitAvailability.AVAILABLE)
                .map(LoanKitOption::from)
                .toList();
    }

    @Transactional
    public void create(CreateLoanRequest request, CustomUserDetails currentUser) {
        if (request.equipmentIds().isEmpty() && request.kitId() == null) {
            throw new BusinessException("Debe seleccionar al menos un equipo o un kit");
        }
        if (!request.scheduledReturnAt().isAfter(request.scheduledPickupAt())) {
            throw new BusinessException("La devolución debe ser posterior al recojo");
        }

        Loan loan = new Loan();
        loan.setCode(RandomCodeGenerator.generateUnique(
                LoanConstants.CODE_PREFIX, LoanConstants.CODE_LENGTH, loanRepository::existsByCode));
        loan.setRequester(findRequester(resolveRequesterId(request, currentUser)));
        loan.setRegisteredBy(findUser(currentUser.getId()));
        loan.setScheduledPickupAt(request.scheduledPickupAt());
        loan.setScheduledReturnAt(request.scheduledReturnAt());
        loan.setNotes(TextUtils.trimToNull(request.notes()));

        Kit kit = request.kitId() == null ? null : findLendableKit(request.kitId());
        loan.setKit(kit);

        List<Equipment> equipments = findLendableEquipments(request.equipmentIds(), kit);
        equipments.forEach(equipment -> {
            LoanItem item = new LoanItem();
            item.setEquipment(equipment);
            loan.addItem(item);
            equipment.setAvailability(EquipmentAvailability.LOANED);
        });

        Loan saved = loanRepository.save(loan);
        log.info("Préstamo registrado: loanId={}, code={}, items={}", saved.getId(), saved.getCode(), equipments.size());
    }

    @Transactional
    public void cancel(Long loanId, CustomUserDetails currentUser) {
        Loan loan = loanRepository.findById(loanId)
                .filter(found -> isManager(currentUser) || found.getRequester().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado"));

        if (loan.getStatus() != LoanStatus.PENDING_PICKUP) {
            throw new BusinessException("Solo se pueden cancelar préstamos pendientes de recojo");
        }

        loan.setStatus(LoanStatus.CANCELLED);
        loan.setCancelledAt(Instant.now());
        loan.getItems().stream()
                .map(LoanItem::getEquipment)
                .filter(equipment -> equipment.getAvailability() == EquipmentAvailability.LOANED)
                .forEach(Equipment::releaseFromLoan);

        log.info("Préstamo cancelado: loanId={}, code={}, by={}", loan.getId(), loan.getCode(), currentUser.getId());
    }

    @Transactional
    public void registerPickup(Long loanId, RegisterPickupRequest request, CustomUserDetails currentUser) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado"));

        if (loan.getStatus() != LoanStatus.PENDING_PICKUP) {
            throw new BusinessException("Solo se puede registrar el recojo de préstamos pendientes");
        }

        request.items().stream()
                .filter(item -> item.condition() == EquipmentCondition.DAMAGED)
                .findFirst()
                .ifPresent(item -> {
                    throw new BusinessException("No se puede entregar un equipo dañado; retíralo del préstamo o cancélalo");
                });
        loan.getItems().forEach(item -> item.setPickupCondition(item.getEquipment().getCondition()));
        applyConditions(loan, request.items(), LoanItem::setPickupCondition);

        loan.setStatus(LoanStatus.ACTIVE);
        loan.setPickedUpAt(Instant.now());
        loan.setDeliveredBy(findUser(currentUser.getId()));

        log.info("Recojo registrado: loanId={}, code={}, by={}", loan.getId(), loan.getCode(), currentUser.getId());
    }

    @Transactional
    public void registerReturn(Long loanId, RegisterReturnRequest request, CustomUserDetails currentUser) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado"));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessException("Solo se puede registrar la devolución de préstamos en curso");
        }

        loan.getItems().forEach(item -> item.setReturnCondition(item.getPickupCondition()));
        applyConditions(loan, request.items(), LoanItem::setReturnCondition);
        loan.getItems().stream()
                .map(LoanItem::getEquipment)
                .filter(equipment -> equipment.getAvailability() == EquipmentAvailability.LOANED)
                .forEach(Equipment::releaseFromLoan);

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnedAt(Instant.now());
        loan.setReceivedBy(findUser(currentUser.getId()));

        log.info("Devolución registrada: loanId={}, code={}, late={}, by={}",
                loan.getId(), loan.getCode(), loan.isReturnedLate(), currentUser.getId());
    }

    private void applyConditions(
            Loan loan, List<LoanItemConditionRequest> conditions, BiConsumer<LoanItem, EquipmentCondition> setter) {
        Map<Long, LoanItem> itemsByEquipmentId = loan.getItems().stream()
                .collect(Collectors.toMap(item -> item.getEquipment().getId(), Function.identity()));

        conditions.forEach(request -> {
            LoanItem item = itemsByEquipmentId.get(request.equipmentId());
            if (item == null) {
                throw new BusinessException("Uno o más equipos no pertenecen al préstamo");
            }
            setter.accept(item, request.condition());
            item.getEquipment().setCondition(request.condition());
        });
    }

    private List<Equipment> findLendableEquipments(List<Long> equipmentIds, Kit kit) {
        Set<Long> ids = new HashSet<>(equipmentIds);
        List<Equipment> selected = equipmentRepository.findAllById(ids);

        if (selected.size() != ids.size()) {
            throw new ResourceNotFoundException("Uno o más equipos seleccionados no existen");
        }

        selected.stream()
                .filter(equipment -> !equipment.isLendable())
                .findFirst()
                .ifPresent(equipment -> {
                    throw new BusinessException("El equipo " + equipment.getName() + " no está disponible para préstamo");
                });

        if (kit == null) {
            return selected;
        }

        Set<Long> kitEquipmentIds = kit.getEquipments().stream()
                .map(Equipment::getId)
                .collect(Collectors.toSet());
        selected.stream()
                .filter(equipment -> kitEquipmentIds.contains(equipment.getId()))
                .findFirst()
                .ifPresent(equipment -> {
                    throw new ConflictException(
                            "El equipo " + equipment.getName() + " ya está incluido en el kit " + kit.getName());
                });

        return Stream.concat(selected.stream(), kit.getEquipments().stream()).toList();
    }

    private Kit findLendableKit(Long kitId) {
        Kit kit = kitRepository.findById(kitId)
                .orElseThrow(() -> new ResourceNotFoundException("Kit no encontrado"));

        if (kit.getAvailability() != KitAvailability.AVAILABLE) {
            throw new BusinessException("El kit " + kit.getName() + " no está disponible para préstamo");
        }
        return kit;
    }

    private Long resolveRequesterId(CreateLoanRequest request, CustomUserDetails currentUser) {
        if (!isManager(currentUser)) {
            return currentUser.getId();
        }
        if (request.requesterId() == null) {
            throw new BusinessException("Seleccione al solicitante");
        }
        return request.requesterId();
    }

    private User findRequester(Long requesterId) {
        User requester = findUser(requesterId);

        if (requester.isSuspended()) {
            throw new BusinessException("El solicitante está suspendido");
        }
        if (!requester.isProfileCompleted()) {
            throw new BusinessException("El solicitante aún no completa su perfil");
        }
        return requester;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Specification<Loan> buildSpecification(String search, LoanFilterRequest filter) {
        Specification<Loan> spec = Specification.unrestricted();

        if (StringUtils.hasText(search)) {
            spec = spec.and(LoanSpecifications.matchesSearch(search.trim()));
        }
        if (!filter.statuses().isEmpty()) {
            spec = spec.and(LoanSpecifications.hasAnyStatus(filter.statuses()));
        }
        if (filter.pickupFrom() != null) {
            spec = spec.and(LoanSpecifications.scheduledPickupFrom(filter.pickupFrom()));
        }
        if (filter.pickupTo() != null) {
            spec = spec.and(LoanSpecifications.scheduledPickupBefore(filter.pickupTo()));
        }
        if (filter.returnFrom() != null) {
            spec = spec.and(LoanSpecifications.scheduledReturnFrom(filter.returnFrom()));
        }
        if (filter.returnTo() != null) {
            spec = spec.and(LoanSpecifications.scheduledReturnBefore(filter.returnTo()));
        }

        return spec;
    }

    private Map<Long, Person> findPeopleByUserId(List<Loan> loans) {
        Set<Long> userIds = loans.stream()
                .flatMap(loan -> Stream.of(loan.getRequester(), loan.getDeliveredBy(), loan.getReceivedBy(), loan.getRegisteredBy()))
                .filter(Objects::nonNull)
                .map(User::getId)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Map.of();
        }
        return personRepository.findByUserIdInAndDeletedAtIsNull(userIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity()));
    }

    private static boolean isManager(CustomUserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> MANAGER_AUTHORITIES.contains(authority.getAuthority()));
    }
}
