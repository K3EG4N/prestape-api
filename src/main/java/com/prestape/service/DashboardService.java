package com.prestape.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.prestape.config.ReportProperties;
import com.prestape.dto.response.AdminDashboardResponse;
import com.prestape.dto.response.AdminDashboardResponse.AttentionEquipment;
import com.prestape.dto.response.AdminDashboardResponse.CategoryStock;
import com.prestape.dto.response.AdminDashboardResponse.EquipmentSummary;
import com.prestape.dto.response.AdminDashboardResponse.KitSummary;
import com.prestape.dto.response.AdminDashboardResponse.RecentUser;
import com.prestape.dto.response.AdminDashboardResponse.Trends;
import com.prestape.dto.response.AdminDashboardResponse.UserSummary;
import com.prestape.dto.types.UserStatusFilter;
import com.prestape.entity.Person;
import com.prestape.entity.Role;
import com.prestape.entity.User;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;
import com.prestape.entity.enums.KitAvailability;
import com.prestape.repository.EquipmentCategoryRepository;
import com.prestape.repository.EquipmentRepository;
import com.prestape.repository.KitRepository;
import com.prestape.repository.PersonRepository;
import com.prestape.repository.UserRepository;
import com.prestape.specifications.KitSpecifications;
import com.prestape.specifications.UserSpecifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentCategoryRepository categoryRepository;
    private final KitRepository kitRepository;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final ReportProperties reportProperties;

    public DashboardService(
            EquipmentRepository equipmentRepository,
            EquipmentCategoryRepository categoryRepository,
            KitRepository kitRepository,
            UserRepository userRepository,
            PersonRepository personRepository,
            ReportProperties reportProperties
    ) {
        this.equipmentRepository = equipmentRepository;
        this.categoryRepository = categoryRepository;
        this.kitRepository = kitRepository;
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.reportProperties = reportProperties;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse admin() {
        EquipmentSummary equipment = equipmentSummary();
        KitSummary kits = kitSummary();
        UserSummary users = userSummary();

        return new AdminDashboardResponse(
                equipment,
                kits,
                categoryStock(),
                users,
                attention(),
                trends(equipment.total(), kits.total(), users.total()));
    }

    private Trends trends(long equipment, long kits, long users) {
        Instant monthStart = monthStart();
        return new Trends(
                percentChange(equipment, equipmentRepository.countExistingAt(monthStart)),
                percentChange(kits, kitRepository.countExistingAt(monthStart)),
                percentChange(users, userRepository.countExistingAt(monthStart)));
    }

    private static Integer percentChange(long current, long previous) {
        if (previous == 0) {
            return null;
        }
        return (int) Math.round((current - previous) * 100.0 / previous);
    }

    private Instant monthStart() {
        return LocalDate.now(reportProperties.timezone())
                .withDayOfMonth(1)
                .atStartOfDay(reportProperties.timezone())
                .toInstant();
    }

    private EquipmentSummary equipmentSummary() {
        Map<EquipmentAvailability, Long> byAvailability = new EnumMap<>(EquipmentAvailability.class);
        for (EquipmentAvailability availability : EquipmentAvailability.values()) {
            byAvailability.put(availability, 0L);
        }
        equipmentRepository.countByAvailability()
                .forEach(row -> byAvailability.put(row.getStatus(), row.getTotal()));

        Map<EquipmentCondition, Long> byCondition = new EnumMap<>(EquipmentCondition.class);
        for (EquipmentCondition condition : EquipmentCondition.values()) {
            byCondition.put(condition, 0L);
        }
        equipmentRepository.countByCondition()
                .forEach(row -> byCondition.put(row.getStatus(), row.getTotal()));

        long total = byAvailability.values().stream().mapToLong(Long::longValue).sum();
        return new EquipmentSummary(total, byAvailability, byCondition);
    }

    private KitSummary kitSummary() {
        return new KitSummary(
                kitRepository.count(),
                kitRepository.count(KitSpecifications.hasAnyAvailability(List.of(KitAvailability.AVAILABLE))));
    }

    private List<CategoryStock> categoryStock() {
        Map<Long, List<String>> namesByCategory = equipmentRepository.findNamesByCategory().stream()
                .collect(Collectors.groupingBy(
                        EquipmentRepository.CategoryEquipmentName::getCategoryId,
                        Collectors.mapping(EquipmentRepository.CategoryEquipmentName::getName, Collectors.toList())));

        return categoryRepository.findStockByCategory().stream()
                .map(row -> new CategoryStock(
                        row.getId(),
                        row.getName(),
                        row.getTotal(),
                        row.getAvailable(),
                        namesByCategory.getOrDefault(row.getId(), List.of())))
                .toList();
    }

    private UserSummary userSummary() {
        Instant monthStart = monthStart();

        return new UserSummary(
                userRepository.count(),
                countUsers(UserStatusFilter.ACTIVE),
                countUsers(UserStatusFilter.INCOMPLETE),
                countUsers(UserStatusFilter.SUSPENDED),
                userRepository.count(UserSpecifications.createdFrom(monthStart)),
                recentUsers());
    }

    private List<RecentUser> recentUsers() {
        List<User> users = userRepository.findTop5ByOrderByCreatedAtDesc();
        Map<Long, Person> peopleByUserId = personRepository
                .findByUserIdInAndDeletedAtIsNull(users.stream().map(User::getId).toList()).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity()));

        return users.stream()
                .map(user -> {
                    Person person = peopleByUserId.get(user.getId());
                    return new RecentUser(
                            user.getId(),
                            person != null ? person.getDisplayName() : null,
                            user.getEmail(),
                            user.getRoles().stream()
                                    .map(Role::getExternalName)
                                    .min(Comparator.naturalOrder())
                                    .orElse(null),
                            user.getStatus(),
                            user.isProfileCompleted(),
                            user.getCreatedAt());
                })
                .toList();
    }

    private long countUsers(UserStatusFilter status) {
        return userRepository.count(UserSpecifications.hasAnyStatus(List.of(status)));
    }

    private List<AttentionEquipment> attention() {
        return equipmentRepository
                .findTop5ByAvailabilityOrConditionOrderByUpdatedAtDesc(
                        EquipmentAvailability.IN_MAINTENANCE, EquipmentCondition.DAMAGED)
                .stream()
                .map(AttentionEquipment::from)
                .toList();
    }
}
