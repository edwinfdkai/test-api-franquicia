package com.edwin.testapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(
        @NotBlank(message = "El nombre de la sucursal es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name
) {
}

