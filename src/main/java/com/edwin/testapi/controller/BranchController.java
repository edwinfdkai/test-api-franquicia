package com.edwin.testapi.controller;

import com.edwin.testapi.dto.request.CreateBranchRequest;
import com.edwin.testapi.dto.request.RenameRequest;
import com.edwin.testapi.dto.response.BranchResponse;
import com.edwin.testapi.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class BranchController {

    private final BranchService branchService;

    // Inyecta el servicio encargado de las sucursales.
    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    // Añade una sucursal a una franquicia.
    @PostMapping("/franchises/{franchiseId}/branches")
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse create(
            @PathVariable Long franchiseId,
            @Valid @RequestBody CreateBranchRequest request) {
        return branchService.create(franchiseId, request);
    }

    // Cambia el nombre de una sucursal existente.
    @PatchMapping("/branches/{id}")
    public BranchResponse rename(
            @PathVariable Long id,
            @Valid @RequestBody RenameRequest request) {
        return branchService.rename(id, request.name());
    }
}
