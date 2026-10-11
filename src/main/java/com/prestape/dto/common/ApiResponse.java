package com.prestape.dto.common;

import java.util.Map;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
		int status,
		boolean success,
		String message,
		T value,
		Map<String, String> errors	
		) {

	public static <T> ApiResponse<T> ok(T value) {
        return new ApiResponse<>(200, true, "OK", value, null);
    }

    public static <T> ApiResponse<T> ok(String message, T value) {
        return new ApiResponse<>(200, true, message, value, null);
    }

    public static <T> ApiResponse<T> created(T value) {
        return new ApiResponse<>(201, true, "Creado correctamente", value, null);
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return new ApiResponse<>(status.value(), false, message, null, null);
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message, Map<String, String> errors) {
        return new ApiResponse<>(status.value(), false, message, null, errors);
    }
}
