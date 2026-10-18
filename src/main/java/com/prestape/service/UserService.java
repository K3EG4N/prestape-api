package com.prestape.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.prestape.dto.request.user.create.CreateUserRequest;
import com.prestape.dto.request.user.update.UpdateUserRequest;
import com.prestape.exception.BusinessException;
import com.prestape.exception.ConflictException;
import com.prestape.exception.ResourceNotFoundException;
import com.prestape.security.Roles;
import com.prestape.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.prestape.dto.common.PageResponse;
import com.prestape.dto.request.user.list.UserListRequest;
import com.prestape.dto.response.UserListResponse;
import com.prestape.entity.Person;
import com.prestape.entity.Role;
import com.prestape.entity.User;
import com.prestape.repository.PersonRepository;
import com.prestape.repository.UserRepository;
import com.prestape.specifications.UserSpecifications;

@Service
public class UserService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonService personService;

    public UserService(
            UserRepository userRepository,
            PersonRepository personRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            PersonService personService
    ) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.personService = personService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserListResponse> list(UserListRequest request) {
        Specification<User> spec = Specification.unrestricted();

        if (StringUtils.hasText(request.search())) {
            spec = spec.and(UserSpecifications.matchesSearch(request.search().trim()));
        }

        List<String> roles = request.roles().stream()
                .filter(StringUtils::hasText)
                .map(r -> r.trim().toUpperCase())
                .toList();
        if (!roles.isEmpty()) {
            spec = spec.and(UserSpecifications.hasAnyRole(roles));
        }

        if (request.createdFrom() != null) {
            spec = spec.and(UserSpecifications.createdFrom(request.createdFrom()));
        }
        if (request.createdTo() != null) {
            spec = spec.and(UserSpecifications.createdBefore(request.createdTo()));
        }

        Pageable pageable = PageRequest.of(request.page() - 1, request.size(), DEFAULT_SORT);
        Page<User> users = userRepository.findAll(spec, pageable);

        Map<Long, Person> peopleByUserId = personRepository
                .findByUserIdInAndDeletedAtIsNull(users.map(User::getId).toList())
                .stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity()));

        return PageResponse.from(users.map(u -> UserListResponse.from(u, peopleByUserId.get(u.getId()))));
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
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
