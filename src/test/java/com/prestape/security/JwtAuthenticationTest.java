package com.prestape.security;

import static com.prestape.support.TestFixtures.STUDENT;
import static com.prestape.support.TestFixtures.principal;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.prestape.controller.LoanController;
import com.prestape.dto.CustomUserDetails;
import com.prestape.report.LoanExcelReport;
import com.prestape.service.LoanService;

@WebMvcTest(LoanController.class)
@Import({SecurityConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "jwt.secret=" + JwtAuthenticationTest.SECRET,
        "jwt.expiration-ms=60000"
})
@DisplayName("Seguridad: autenticación con JWT")
class JwtAuthenticationTest {

    static final String SECRET = "clave-de-pruebas-para-prestape-de-32-bytes-o-mas";
    private static final CustomUserDetails STUDENT_USER = principal(10L, STUDENT);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;
    @MockitoBean
    private LoanExcelReport loanExcelReport;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(userDetailsService.loadUserByUsername(STUDENT_USER.getUsername())).thenReturn(STUDENT_USER);
    }

    @Test
    @DisplayName("Rechaza un token firmado con otra clave")
    void rejectsForgedToken() throws Exception {
        String forged = new JwtService("clave-de-un-atacante-que-no-conoce-el-secreto", 60_000)
                .generateToken(STUDENT_USER);

        mockMvc.perform(get("/api/loans").header(HttpHeaders.AUTHORIZATION, "Bearer " + forged))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(loanService);
    }

    @Test
    @DisplayName("Rechaza un token vencido")
    void rejectsExpiredToken() throws Exception {
        String expired = new JwtService(SECRET, -1_000).generateToken(STUDENT_USER);

        mockMvc.perform(get("/api/loans").header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(loanService);
    }
}
