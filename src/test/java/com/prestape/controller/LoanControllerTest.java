package com.prestape.controller;

import static com.prestape.support.TestFixtures.STUDENT;
import static com.prestape.support.TestFixtures.principal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.prestape.dto.CustomUserDetails;
import com.prestape.report.LoanExcelReport;
import com.prestape.security.CustomUserDetailsService;
import com.prestape.security.JwtService;
import com.prestape.security.SecurityConfig;
import com.prestape.service.LoanService;

@WebMvcTest(LoanController.class)
@Import(SecurityConfig.class)
@DisplayName("API de préstamos")
class LoanControllerTest {

    private static final CustomUserDetails STUDENT_USER = principal(10L, STUDENT);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;
    @MockitoBean
    private LoanExcelReport loanExcelReport;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("Valida las fechas obligatorias del préstamo")
    void validatesRequiredDates() throws Exception {
        mockMvc.perform(post("/api/loans").with(user(STUDENT_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equipmentIds\": [1]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Datos inválidos"))
                .andExpect(jsonPath("$.errors.scheduledPickupAt").value("La fecha de recojo es obligatoria"))
                .andExpect(jsonPath("$.errors.scheduledReturnAt").value("La fecha de devolución es obligatoria"));

        verifyNoInteractions(loanService);
    }

    @Test
    @DisplayName("Un estudiante no puede registrar el recojo")
    void studentCannotRegisterPickup() throws Exception {
        mockMvc.perform(put("/api/loans/1/pickup").with(user(STUDENT_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\": []}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No tienes permisos para esta acción"));

        verify(loanService, never()).registerPickup(any(), any(), any());
    }
}
