package com.prestape.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prestape.config.AppProperties;
import com.prestape.constants.SettingsConstants;
import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.loan.LoanLimit;
import com.prestape.dto.loan.LoanRules;
import com.prestape.dto.request.settings.BusinessDayRequest;
import com.prestape.dto.request.settings.RoleLimitRequest;
import com.prestape.dto.request.settings.UpdateSettingsRequest;
import com.prestape.dto.response.SettingsResponse;
import com.prestape.entity.BusinessHour;
import com.prestape.entity.Person;
import com.prestape.entity.Role;
import com.prestape.entity.RoleLoanLimit;
import com.prestape.entity.SystemSettings;
import com.prestape.entity.User;
import com.prestape.exception.custom.BusinessException;
import com.prestape.exception.custom.ResourceNotFoundException;
import com.prestape.repository.BusinessHourRepository;
import com.prestape.repository.PersonRepository;
import com.prestape.repository.RoleLoanLimitRepository;
import com.prestape.repository.RoleRepository;
import com.prestape.repository.SystemSettingsRepository;
import com.prestape.repository.UserRepository;

@Service
public class SettingsService {
    private static final Logger log = LoggerFactory.getLogger(SettingsService.class);
    private final SystemSettingsRepository settingsRepository;
    private final RoleLoanLimitRepository roleLoanLimitRepository;
    private final BusinessHourRepository businessHourRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final AppProperties appProperties;

    public SettingsService(
            SystemSettingsRepository settingsRepository,
            RoleLoanLimitRepository roleLoanLimitRepository,
            BusinessHourRepository businessHourRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PersonRepository personRepository,
            AppProperties appProperties
    ) {
        this.settingsRepository = settingsRepository;
        this.roleLoanLimitRepository = roleLoanLimitRepository;
        this.businessHourRepository = businessHourRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.appProperties = appProperties;
    }

    @Transactional(readOnly = true)
    public LoanRules loanRules() {
        SystemSettings settings = findSettings();
        return new LoanRules(
                Duration.ofMinutes(settings.getToleranceMinutes()),
                Duration.ofDays(settings.getMaxDurationDays()),
                Duration.ofHours(settings.getMinAdvanceHours()),
                Duration.ofHours(settings.getAutoCancelHours()),
                businessHourRepository.findAllByOrderByDayOfWeekAsc(),
                appProperties.timezone());
    }

    @Transactional(readOnly = true)
    public Duration tolerance() {
        return Duration.ofMinutes(findSettings().getToleranceMinutes());
    }

    @Transactional(readOnly = true)
    public Optional<LoanLimit> limitFor(User user) {
        boolean unlimited = user.getRoles().stream()
                .anyMatch(role -> SettingsConstants.UNLIMITED_ROLES.contains(role.getName().toUpperCase()));
        if (unlimited) {
            return Optional.empty();
        }

        Map<Long, RoleLoanLimit> limitsByRoleId = roleLoanLimitRepository.findAll().stream()
                .collect(Collectors.toMap(RoleLoanLimit::getRoleId, Function.identity()));
        List<LoanLimit> limits = user.getRoles().stream()
                .map(role -> toLoanLimit(limitsByRoleId.get(role.getId())))
                .toList();

        return Optional.of(new LoanLimit(
                limits.stream().mapToInt(LoanLimit::maxEquipments).max()
                        .orElse(SettingsConstants.DEFAULT_MAX_EQUIPMENTS),
                limits.stream().mapToInt(LoanLimit::maxActiveLoans).max()
                        .orElse(SettingsConstants.DEFAULT_MAX_ACTIVE_LOANS)));
    }

    @Transactional(readOnly = true)
    public SettingsResponse get() {
        SystemSettings settings = findSettings();
        Map<Long, RoleLoanLimit> limitsByRoleId = roleLoanLimitRepository.findAll().stream()
                .collect(Collectors.toMap(RoleLoanLimit::getRoleId, Function.identity()));

        List<SettingsResponse.RoleLimitInfo> roleLimits = findLimitedRoles().stream()
                .map(role -> toRoleLimitInfo(role, limitsByRoleId.get(role.getId())))
                .toList();
        List<SettingsResponse.BusinessDayInfo> businessHours = businessHourRepository.findAllByOrderByDayOfWeekAsc()
                .stream()
                .map(SettingsResponse.BusinessDayInfo::from)
                .toList();

        return new SettingsResponse(
                settings.getToleranceMinutes(),
                settings.getMaxDurationDays(),
                settings.getMinAdvanceHours(),
                settings.getAutoCancelHours(),
                roleLimits,
                businessHours,
                settings.getUpdatedAt(),
                displayName(settings.getUpdatedBy()));
    }

    @Transactional
    public void update(UpdateSettingsRequest request, CustomUserDetails currentUser) {
        validateBusinessDays(request.businessHours());
        Map<Long, Role> limitedRoles = findLimitedRoles().stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));
        validateRoleLimits(request.roleLimits(), limitedRoles.keySet());

        SystemSettings settings = findSettings();
        settings.setToleranceMinutes(request.toleranceMinutes());
        settings.setMaxDurationDays(request.maxDurationDays());
        settings.setMinAdvanceHours(request.minAdvanceHours());
        settings.setAutoCancelHours(request.autoCancelHours());
        settings.setUpdatedAt(Instant.now());
        settings.setUpdatedBy(userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado")));

        updateRoleLimits(request.roleLimits(), limitedRoles);
        updateBusinessHours(request.businessHours());

        log.info("Configuración actualizada: by={}", currentUser.getId());
    }

    private void updateRoleLimits(List<RoleLimitRequest> requests, Map<Long, Role> limitedRoles) {
        Map<Long, RoleLoanLimit> limitsByRoleId = roleLoanLimitRepository.findAll().stream()
                .collect(Collectors.toMap(RoleLoanLimit::getRoleId, Function.identity()));

        List<RoleLoanLimit> limits = requests.stream()
                .map(request -> {
                    RoleLoanLimit limit = limitsByRoleId.get(request.roleId());
                    if (limit == null) {
                        limit = new RoleLoanLimit();
                        limit.setRole(limitedRoles.get(request.roleId()));
                    }
                    limit.setMaxEquipments(request.maxEquipments());
                    limit.setMaxActiveLoans(request.maxActiveLoans());
                    return limit;
                })
                .toList();

        roleLoanLimitRepository.saveAll(limits);
    }

    private void updateBusinessHours(List<BusinessDayRequest> requests) {
        Map<Short, BusinessHour> hoursByDay = businessHourRepository.findAll().stream()
                .collect(Collectors.toMap(BusinessHour::getDayOfWeek, Function.identity()));

        List<BusinessHour> hours = requests.stream()
                .map(request -> {
                    BusinessHour hour = hoursByDay.get(request.day());
                    if (hour == null) {
                        hour = new BusinessHour();
                        hour.setDayOfWeek(request.day());
                    }
                    hour.setOpen(request.open());
                    hour.setOpensAt(request.opensAt());
                    hour.setClosesAt(request.closesAt());
                    return hour;
                })
                .toList();

        businessHourRepository.saveAll(hours);
    }

    private void validateBusinessDays(List<BusinessDayRequest> days) {
        Set<Short> uniqueDays = days.stream().map(BusinessDayRequest::day).collect(Collectors.toSet());
        if (uniqueDays.size() != SettingsConstants.DAYS_IN_WEEK) {
            throw new BusinessException("El horario debe incluir cada día de la semana una sola vez");
        }
        if (days.stream().noneMatch(BusinessDayRequest::open)) {
            throw new BusinessException("El laboratorio debe atender al menos un día a la semana");
        }
    }

    private void validateRoleLimits(List<RoleLimitRequest> limits, Set<Long> limitedRoleIds) {
        Set<Long> seen = new HashSet<>();
        limits.forEach(limit -> {
            if (!limitedRoleIds.contains(limit.roleId())) {
                throw new BusinessException("Uno de los roles no admite límites de préstamo");
            }
            if (!seen.add(limit.roleId())) {
                throw new BusinessException("Cada rol solo puede tener un límite");
            }
        });
    }

    private List<Role> findLimitedRoles() {
        return roleRepository.findAll().stream()
                .filter(role -> !SettingsConstants.UNLIMITED_ROLES.contains(role.getName().toUpperCase()))
                .sorted(Comparator.comparing(Role::getExternalName))
                .toList();
    }

    private SettingsResponse.RoleLimitInfo toRoleLimitInfo(Role role, RoleLoanLimit limit) {
        return new SettingsResponse.RoleLimitInfo(
                role.getId(),
                role.getName(),
                role.getExternalName(),
                limit != null ? limit.getMaxEquipments() : SettingsConstants.DEFAULT_MAX_EQUIPMENTS,
                limit != null ? limit.getMaxActiveLoans() : SettingsConstants.DEFAULT_MAX_ACTIVE_LOANS);
    }

    private LoanLimit toLoanLimit(RoleLoanLimit limit) {
        if (limit == null) {
            return new LoanLimit(SettingsConstants.DEFAULT_MAX_EQUIPMENTS, SettingsConstants.DEFAULT_MAX_ACTIVE_LOANS);
        }
        return new LoanLimit(limit.getMaxEquipments(), limit.getMaxActiveLoans());
    }

    private String displayName(User user) {
        if (user == null) {
            return null;
        }
        return personRepository.findByUserIdAndDeletedAtIsNull(user.getId())
                .map(Person::getShortName)
                .orElse(user.getEmail());
    }

    private SystemSettings findSettings() {
        return settingsRepository.findById(SettingsConstants.SETTINGS_ID)
                .orElseThrow(() -> new ResourceNotFoundException("La configuración del sistema no existe"));
    }
}
