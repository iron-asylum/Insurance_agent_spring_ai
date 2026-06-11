package com.insurance.claims.controller;

import com.insurance.claims.model.Claim;
import com.insurance.claims.model.ClaimDocument;
import com.insurance.claims.model.ClaimStatus;
import com.insurance.claims.repository.ClaimDocumentRepository;
import com.insurance.claims.repository.ClaimRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository documentRepository;

    public ClaimController(ClaimRepository claimRepository, ClaimDocumentRepository documentRepository) {
        this.claimRepository = claimRepository;
        this.documentRepository = documentRepository;
    }

    @PostMapping
    public ResponseEntity<Claim> create(@Valid @RequestBody Claim claim) {
        Claim saved = claimRepository.save(claim);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<Claim> list(@RequestParam(required = false) ClaimStatus status) {
        return status == null ? claimRepository.findAll() : claimRepository.findByStatus(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Claim> get(@PathVariable Long id) {
        return claimRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/document")
    public ResponseEntity<ClaimDocument> document(@PathVariable Long id) {
        return claimRepository.findById(id)
                .flatMap(c -> documentRepository.findByClaimReference(c.getReference()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
