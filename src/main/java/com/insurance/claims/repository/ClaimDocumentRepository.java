package com.insurance.claims.repository;

import com.insurance.claims.model.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Long> {

    Optional<ClaimDocument> findByClaimReference(String claimReference);
}
