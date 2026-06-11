package com.insurance.claims.tool;

import com.insurance.claims.model.Claim;
import com.insurance.claims.repository.ClaimRepository;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class ClaimHistoryTool implements AgentTool {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ClaimRepository claimRepository;

    public ClaimHistoryTool(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    public String name() {
        return "get_claim_history";
    }

    @Override
    public String description() {
        return "Get the past claim history (claim id, policy number, description, amount, status, date) for a policy holder by full name.";
    }

    @Override
    public List<ToolParam> parameters() {
        return List.of(
                new ToolParam("policyHolderName", "Full name of the policy holder, e.g. John Smith", true)
        );
    }

    @Override
    public String execute(Map<String, String> args) {
        return getClaimHistory(args.get("policyHolderName"));
    }

    public String getClaimHistory(String policyHolderName) {
        if (policyHolderName == null || policyHolderName.isBlank()) {
            return "Error: policyHolderName is required.";
        }
        List<Claim> claims = claimRepository.findByClaimantNameContainingIgnoreCase(policyHolderName.trim());

        if (claims.isEmpty()) {
            return "No claims found for policy holder: " + policyHolderName;
        }

        StringBuilder result = new StringBuilder();
        result.append("Claim History for ").append(policyHolderName).append("\n");
        result.append("==================================\n");

        for (Claim claim : claims) {
            result.append("Claim ID: ").append(claim.getReference() != null ? claim.getReference() : claim.getId()).append("\n");
            result.append("Policy Number: ").append(claim.getPolicyNumber()).append("\n");
            result.append("Description: ").append(claim.getClaimDescription()).append("\n");
            result.append("Amount: $").append(claim.getClaimAmount()).append("\n");
            result.append("Status: ").append(claim.getStatus()).append("\n");
            result.append("Date: ").append(claim.getCreatedAt() != null ? claim.getCreatedAt().format(DATE) : "n/a").append("\n");
            result.append("------------------------\n");
        }

        return result.toString();
    }
}
