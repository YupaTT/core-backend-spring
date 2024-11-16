package com.core.backend.common.api;

import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull Boolean activo) {}
