package com.edwin.testapi.dto.response;

public record HighestStockProductResponse(
        Long branchId,
        String branchName,
        Long productId,
        String productName,
        Integer stock
) {
}
