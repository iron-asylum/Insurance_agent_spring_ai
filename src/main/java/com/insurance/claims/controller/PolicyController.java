package com.insurance.claims.controller;

import com.insurance.claims.model.Claim;
import com.insurance.claims.model.Policy;
import com.insurance.claims.repository.ClaimRepository;
import com.insurance.claims.repository.PolicyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    public PolicyController(PolicyRepository policyRepository, ClaimRepository claimRepository) {
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    @GetMapping
    public List<Policy> list() {
        return policyRepository.findAll();
    }

    @GetMapping("/{policyNumber}")
    public ResponseEntity<Policy> get(@PathVariable String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{policyNumber}/claims")
    public List<Claim> claims(@PathVariable String policyNumber) {
        return claimRepository.findByPolicyNumber(policyNumber);
    }
}
