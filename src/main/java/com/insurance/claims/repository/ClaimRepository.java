package com.insurance.claims.repository;

import com.insurance.claims.model.Claim;
import com.insurance.claims.model.ClaimStatus;
import com.insurance.claims.model.ClaimState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    
    List<Claim> findByStatus(ClaimStatus status);
    
    List<Claim> findByState(ClaimState state);
    
    List<Claim> findByPolicyNumber(String policyNumber);
    
    List<Claim> findByClaimantNameContainingIgnoreCase(String claimantName);

    Optional<Claim> findByReference(String reference);

    Optional<Claim> findByPolicyNumberAndClaimantName(String policyNumber, String claimantName);
    
    @Query("SELECT c FROM Claim c WHERE c.status = :status AND c.state = :state")
    List<Claim> findByStatusAndState(@Param("status") ClaimStatus status, @Param("state") ClaimState state);
    
    @Query("SELECT c FROM Claim c WHERE c.assignedTo = :assignedTo")
    List<Claim> findByAssignedTo(@Param("assignedTo") String assignedTo);
}