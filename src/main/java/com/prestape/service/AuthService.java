package com.prestape.service;

import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.request.LoginRequest;
import com.prestape.dto.request.RegisterRequest;
import com.prestape.dto.response.LoginResponse;
import com.prestape.dto.response.RegisterResponse;
import com.prestape.entity.Role;
import com.prestape.entity.User;
import com.prestape.exception.custom.BusinessException;
import com.prestape.exception.custom.ConflictException;
import com.prestape.exception.custom.TooManyRequestsException;
import com.prestape.repository.RoleRepository;
import com.prestape.repository.UserRepository;
import com.prestape.constants.SecurityConstants;
import com.prestape.security.JwtService;
import com.prestape.security.LoginAttemptService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            LoginAttemptService loginAttemptService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
    }

    public LoginResponse login(LoginRequest request) {
        if (loginAttemptService.isBlocked(request.email())) {
            throw new TooManyRequestsException("Demasiados intentos fallidos. Intenta nuevamente en "
                    + SecurityConstants.LOGIN_LOCK_DURATION.toMinutes() + " minutos");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFailed(request.email());
            throw ex;
        }
        loginAttemptService.loginSucceeded(request.email());

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        assert user != null;
        log.info("Inicio de sesión: userId={}", user.getId());
        return new LoginResponse(jwtService.generateToken(user));
    }

    public RegisterResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException("Las contraseñas no coinciden");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("El email ya está registrado");
        }

        Role role = roleRepository.findByNameIgnoreCase(request.type().name())
                .orElseThrow(() -> new BusinessException("El tipo de usuario no es válido"));

        User newUser = new User(null, request.email(), passwordEncoder.encode(request.password()));
        newUser.setRoles(new HashSet<>(Set.of(role)));

        User saved = userRepository.save(newUser);

        var authorities = saved.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().toUpperCase()))
                .toList();

        CustomUserDetails userDetails = new CustomUserDetails(
                saved.getId(),
                saved.getEmail(),
                saved.getPassword(),
                authorities);

        log.info("Registro de usuario: userId={}", saved.getId());
        return new RegisterResponse(jwtService.generateToken(userDetails));
    }
}
