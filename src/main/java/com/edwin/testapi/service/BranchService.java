package com.edwin.testapi.service;

import com.edwin.testapi.dto.request.CreateBranchRequest;
import com.edwin.testapi.dto.response.BranchResponse;
import com.edwin.testapi.entity.Branch;
import com.edwin.testapi.entity.Franchise;
import com.edwin.testapi.exception.ResourceNotFoundException;
import com.edwin.testapi.repository.BranchRepository;
import com.edwin.testapi.repository.FranchiseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final FranchiseRepository franchiseRepository;

    public BranchService(
            BranchRepository branchRepository,
            FranchiseRepository franchiseRepository) {
        this.branchRepository = branchRepository;
        this.franchiseRepository = franchiseRepository;
    }

    /** Gestiona las sucursales y su relación con las franquicias. */
    @Transactional
    public BranchResponse create(Long franchiseId, CreateBranchRequest request) {
        Franchise franchise = franchiseRepository.findById(franchiseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Franquicia no encontrada con id: " + franchiseId));

        Branch branch = new Branch();
        branch.setName(request.name());
        branch.setFranchise(franchise);

        Branch saved = branchRepository.save(branch);

        return new BranchResponse(
                saved.getId(),
                saved.getName(),
                franchise.getId()
        );
    }

    @Transactional
    public BranchResponse rename(Long id, String name) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal no encontrada con id: " + id));

        branch.setName(name);

        Branch updated = branchRepository.save(branch);

        return new BranchResponse(
                updated.getId(),
                updated.getName(),
                updated.getFranchise().getId()
        );
    }
}
