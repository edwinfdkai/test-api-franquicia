package com.edwin.testapi.dto.response;

public record ProductResponse(
        Long id,
        String name,
        Integer stock,
        Long branchId
) {
}
