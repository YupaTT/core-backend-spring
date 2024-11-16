package com.core.backend.common.api;

public record ApiResponse<T>(boolean finalizado, String estado, String mensaje, T datos) {

    public static <T> ApiResponse<T> ok(String mensaje, T datos) {
        return new ApiResponse<>(true, "OK", mensaje, datos);
    }

    public static <T> ApiResponse<T> ok(T datos) {
        return ok("Operación exitosa", datos);
    }
}
