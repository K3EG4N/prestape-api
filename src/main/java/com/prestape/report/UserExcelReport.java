package com.prestape.report;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.prestape.dto.response.PersonSummaryResponse;
import com.prestape.dto.response.UserListResponse;
import com.prestape.entity.enums.UserStatus;
import com.prestape.report.configuration.ExcelColumnConfiguration;
import com.prestape.report.configuration.ReportFileConfiguration;
import com.prestape.util.TextUtils;

@Component
public class UserExcelReport {
    private final ExcelReportWriter writer;

    public UserExcelReport(ExcelReportWriter writer) {
        this.writer = writer;
    }

    public ReportFileConfiguration generate(List<UserListResponse> users) {
        return writer.write("Usuarios", "reporte_usuarios_", columns(), users);
    }

    private List<ExcelColumnConfiguration<UserListResponse>> columns() {
        return List.of(
                new ExcelColumnConfiguration<>("Código", user -> person(user, PersonSummaryResponse::code)),
                new ExcelColumnConfiguration<>("Nombre completo", user -> person(user, this::fullName)),
                new ExcelColumnConfiguration<>("DNI", user -> person(user, PersonSummaryResponse::documentNumber)),
                new ExcelColumnConfiguration<>("Celular", user -> person(user, PersonSummaryResponse::phone)),
                new ExcelColumnConfiguration<>("Correo", UserListResponse::email),
                new ExcelColumnConfiguration<>("Rol", this::roleSummary),
                new ExcelColumnConfiguration<>("Estado", this::statusLabel),
                new ExcelColumnConfiguration<>("Fecha de registro", UserListResponse::createdAt)
        );
    }

    private Object person(UserListResponse user, Function<PersonSummaryResponse, Object> value) {
        return user.person() != null ? value.apply(user.person()) : null;
    }

    private String fullName(PersonSummaryResponse person) {
        return Stream.of(person.firstName(), person.middleName(), person.paternalSurname(), person.maternalSurname())
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.joining(" "));
    }

    private String roleSummary(UserListResponse user) {
        return user.roles().stream()
                .map(role -> TextUtils.capitalize(role.externalName()))
                .collect(Collectors.joining(", "));
    }

    private String statusLabel(UserListResponse user) {
        if (user.status() == UserStatus.ACTIVE && !user.profileCompleted()) return "Perfil incompleto";
        return user.status().getLabel();
    }
}
