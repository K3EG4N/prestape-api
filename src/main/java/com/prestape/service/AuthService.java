package com.prestape.service;

import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.request.LoginRequest;
import com.prestape.dto.request.RegisterRequest;
import com.prestape.dto.response.LoginResponse;
import com.prestape.dto.response.RegisterResponse;
import com.prestape.entity.Role;
import com.prestape.entity.User;
import com.prestape.exception.BusinessException;
import com.prestape.exception.ConflictException;
import com.prestape.repository.RoleRepository;
import com.prestape.repository.UserRepository;
import com.prestape.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        assert user != null;
        return new LoginResponse(jwtService.generateToken(user));
    }

    public RegisterResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException("Las contraseñas no coinciden");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("El email ya está registrado");
        }

        Role role = roleRepository.findByName(request.type().name())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        User newUser = new User(null, request.email(), passwordEncoder.encode(request.password()));
        newUser.setRoles(Set.of(role));

        User saved = userRepository.save(newUser);

        var authorities = saved.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().toUpperCase()))
                .toList();

        CustomUserDetails userDetails = new CustomUserDetails(
                saved.getId(),
                saved.getEmail(),
                saved.getPassword(),
                authorities);

        return new RegisterResponse(jwtService.generateToken(userDetails));
    }
}
