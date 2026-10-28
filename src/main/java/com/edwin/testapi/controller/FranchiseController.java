package com.edwin.testapi.controller;

import com.edwin.testapi.dto.request.CreateFranchiseRequest;
import com.edwin.testapi.dto.request.RenameRequest;
import com.edwin.testapi.dto.response.FranchiseResponse;
import com.edwin.testapi.service.FranchiseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/franchises")
public class FranchiseController {

    private final FranchiseService franchiseService;

    // Inyecta el servicio encargado de las franquicias.
    public FranchiseController(FranchiseService franchiseService) {
        this.franchiseService = franchiseService;
    }

    // Crea una franquicia nueva.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FranchiseResponse create(
            @Valid @RequestBody CreateFranchiseRequest request) {
        return franchiseService.create(request);
    }

    // Cambia el nombre de una franquicia existente.
    @PatchMapping("/{id}")
    public FranchiseResponse rename(
            @PathVariable Long id,
            @Valid @RequestBody RenameRequest request) {
        return franchiseService.rename(id, request.name());
    }
}
