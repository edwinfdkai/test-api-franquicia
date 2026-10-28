package com.edwin.testapi.service;

import com.edwin.testapi.dto.request.CreateFranchiseRequest;
import com.edwin.testapi.dto.response.FranchiseResponse;
import com.edwin.testapi.entity.Franchise;
import com.edwin.testapi.exception.ResourceNotFoundException;
import com.edwin.testapi.repository.FranchiseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FranchiseService {

    private final FranchiseRepository franchiseRepository;

    public FranchiseService(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    /** Gestiona la creación, consulta y actualización de franquicias. */
    @Transactional
    public FranchiseResponse create(CreateFranchiseRequest request) {
        Franchise franchise = new Franchise();
        franchise.setName(request.name());

        Franchise saved = franchiseRepository.save(franchise);

        return new FranchiseResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public FranchiseResponse rename(Long id, String name) {
        Franchise franchise = franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Franquicia no encontrada con id: " + id));

        franchise.setName(name);

        Franchise updated = franchiseRepository.save(franchise);

        return new FranchiseResponse(updated.getId(), updated.getName());
    }
}