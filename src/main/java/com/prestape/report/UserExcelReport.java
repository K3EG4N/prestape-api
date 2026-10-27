package com.prestape.report;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.prestape.dto.response.PersonSummaryResponse;
import com.prestape.dto.response.UserListResponse;
import com.prestape.entity.enums.UserStatus;

@Component
public class UserExcelReport {

    private static final String SHEET_NAME = "Usuarios";
    private static final String FILE_PREFIX = "reporte_usuarios_";
    private static final List<ExcelColumn<UserListResponse>> COLUMNS = List.of(
            new ExcelColumn<>("Código", u -> person(u, PersonSummaryResponse::code)),
            new ExcelColumn<>("Nombre completo", u -> person(u, UserExcelReport::fullName)),
            new ExcelColumn<>("DNI", u -> person(u, PersonSummaryResponse::documentNumber)),
            new ExcelColumn<>("Celular", u -> person(u, PersonSummaryResponse::phone)),
            new ExcelColumn<>("Correo", UserListResponse::email),
            new ExcelColumn<>("Rol", u -> u.roles().stream()
                    .map(role -> capitalize(role.externalName()))
                    .collect(Collectors.joining(", "))),
            new ExcelColumn<>("Estado", UserExcelReport::statusLabel),
            new ExcelColumn<>("Fecha de registro", UserListResponse::createdAt)
    );

    private final ExcelReportWriter writer;

    public UserExcelReport(ExcelReportWriter writer) {
        this.writer = writer;
    }

    public ReportFile generate(List<UserListResponse> users) {
        return writer.write(SHEET_NAME, FILE_PREFIX, COLUMNS, users);
    }

    private static Object person(UserListResponse user, Function<PersonSummaryResponse, Object> value) {
        return user.person() != null ? value.apply(user.person()) : null;
    }

    private static String fullName(PersonSummaryResponse person) {
        return Stream.of(person.firstName(), person.middleName(), person.paternalSurname(), person.maternalSurname())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) return "";
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private static String statusLabel(UserListResponse user) {
        if (user.status() == UserStatus.SUSPENDED) return "Suspendido";
        return user.profileCompleted() ? "Activo" : "Perfil incompleto";
    }
}
