package com.edwin.testapi.dto.response;

public record BranchResponse(
        Long id,
        String name,
        Long franchiseId
) {
}
