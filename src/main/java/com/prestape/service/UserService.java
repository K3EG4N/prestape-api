package com.prestape.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.prestape.constants.UserConstants;
import com.prestape.config.ReportProperties;
import com.prestape.dto.request.person.create.CreatePersonRequest;
import com.prestape.dto.request.user.create.CreateUserRequest;
import com.prestape.dto.response.MeResponse;
import com.prestape.dto.request.user.update.UpdateUserRequest;
import com.prestape.exception.BusinessException;
import com.prestape.exception.ConflictException;
import com.prestape.exception.ResourceNotFoundException;
import com.prestape.security.Roles;
import com.prestape.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.request.user.list.UserFilterRequest;
import com.prestape.dto.response.UserListResponse;
import com.prestape.entity.Person;
import com.prestape.entity.Role;
import com.prestape.entity.User;
import com.prestape.repository.PersonRepository;
import com.prestape.repository.UserRepository;
import com.prestape.specifications.UserSpecifications;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonService personService;
    private final ReportProperties reportProperties;

    public UserService(
            UserRepository userRepository,
            PersonRepository personRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            PersonService personService,
            ReportProperties reportProperties
    ) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.personService = personService;
        this.reportProperties = reportProperties;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserListResponse> list(PaginationRequest pagination, UserFilterRequest filter) {
        Page<User> users = userRepository.findAll(
                buildSpecification(pagination.search(), filter), pagination.toPageable(UserConstants.DEFAULT_SORT));

        Map<Long, Person> peopleByUserId = findPeopleByUserId(users.map(User::getId).toList());

        return PageResponse.from(users.map(u -> UserListResponse.from(u, peopleByUserId.get(u.getId()))));
    }

    @Transactional(readOnly = true)
    public List<UserListResponse> listForExport(String search, UserFilterRequest filter) {
        Specification<User> spec = buildSpecification(search, filter);

        int maxRows = reportProperties.maxRows();
        if (userRepository.count(spec) > maxRows) {
            throw new BusinessException(
                    "La exportación supera las " + maxRows + " filas. Aplica más filtros");
        }

        List<User> users = userRepository.findAll(spec, UserConstants.DEFAULT_SORT);
        Map<Long, Person> peopleByUserId = findPeopleByUserId(users.stream().map(User::getId).toList());

        return users.stream()
                .map(u -> UserListResponse.from(u, peopleByUserId.get(u.getId())))
                .toList();
    }

    private Map<Long, Person> findPeopleByUserId(List<Long> userIds) {
        return personRepository.findByUserIdInAndDeletedAtIsNull(userIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity()));
    }

    private Specification<User> buildSpecification(String search, UserFilterRequest filter) {
        Specification<User> spec = Specification.unrestricted();

        if (StringUtils.hasText(search)) {
            spec = spec.and(UserSpecifications.matchesSearch(search.trim()));
        }

        List<String> roles = filter.roles().stream()
                .filter(StringUtils::hasText)
                .map(r -> r.trim().toUpperCase())
                .toList();
        if (!roles.isEmpty()) {
            spec = spec.and(UserSpecifications.hasAnyRole(roles));
        }

        if (filter.createdFrom() != null) {
            spec = spec.and(UserSpecifications.createdFrom(filter.createdFrom()));
        }
        if (filter.createdTo() != null) {
            spec = spec.and(UserSpecifications.createdBefore(filter.createdTo()));
        }

        if (!filter.statuses().isEmpty()) {
            spec = spec.and(UserSpecifications.hasAnyStatus(filter.statuses()));
        }

        return spec;
    }

    @Transactional
    public void create(CreateUserRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException("Las contraseñas no coinciden");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("El email ya está registrado");
        }

        Role role = roleRepository.findById(request.role())
                .orElseThrow(() -> new BusinessException("El rol no existe"));

        if (Roles.ADMIN.equalsIgnoreCase(role.getName())) {
            throw new BusinessException("No se puede asignar el rol de administrador al crear un usuario");
        }

        User newUser = new User(null, request.email(), passwordEncoder.encode(request.password()));
        newUser.setRoles(new HashSet<>(Set.of(role)));

        User saved = userRepository.save(newUser);
        personService.createForUser(saved, request.person());
        log.info("Usuario creado: userId={}, role={}", saved.getId(), role.getName());
    }

    @Transactional
    public void update(Long id, UpdateUserRequest request) {
        User user = findUser(id);

        Person person = personService.findActiveByUserId(id)
                .orElseThrow(() -> new BusinessException(
                        "El usuario no tiene un perfil registrado. Complete el perfil antes de editarlo"));

        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictException("El email ya está registrado");
        }

        user.setEmail(request.email());
        personService.update(person, request.person());
        log.info("Usuario actualizado: userId={}", id);
    }

    @Transactional
    public void suspend(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BusinessException("No puedes suspender tu propia cuenta");
        }

        User user = findUser(id);

        if (user.getRoles().stream().anyMatch(r -> Roles.ADMIN.equalsIgnoreCase(r.getName()))) {
            throw new BusinessException("No se puede suspender a un administrador");
        }

        if (user.isSuspended()) {
            throw new ConflictException("El usuario ya está suspendido");
        }

        user.suspend();
        log.info("Usuario suspendido: userId={}, por userId={}", id, currentUserId);
    }

    @Transactional
    public void reactivate(Long id) {
        User user = findUser(id);

        if (!user.isSuspended()) {
            throw new ConflictException("El usuario no está suspendido");
        }

        user.reactivate();
        log.info("Usuario reactivado: userId={}", id);
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(Long userId) {
        User user = findUser(userId);
        Person person = personService.findActiveByUserId(userId).orElse(null);
        return MeResponse.from(user, person);
    }

    @Transactional
    public void completeProfile(Long userId, CreatePersonRequest request) {
        personService.createForUser(findUser(userId), request);
        log.info("Perfil completado: userId={}", userId);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
