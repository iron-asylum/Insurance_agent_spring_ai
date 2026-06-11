package com.insurance.claims.tool;

import com.insurance.claims.model.Policy;
import com.insurance.claims.repository.PolicyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PolicyLookupTool implements AgentTool {

    private final PolicyRepository policyRepository;

    public PolicyLookupTool(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    public String name() {
        return "lookup_policy";
    }

    @Override
    public String description() {
        return "Look up policy details (holder, coverage type, coverage amount, premium, dates, status) by policy number.";
    }

    @Override
    public List<ToolParam> parameters() {
        return List.of(
                new ToolParam("policyNumber", "The policy number, e.g. POL123456", true)
        );
    }

    @Override
    public String execute(Map<String, String> args) {
        return lookupPolicy(args.get("policyNumber"));
    }

    public String lookupPolicy(String policyNumber) {
        if (policyNumber == null || policyNumber.isBlank()) {
            return "Error: policyNumber is required.";
        }
        Optional<Policy> found = policyRepository.findByPolicyNumber(policyNumber.trim());
        if (found.isEmpty()) {
            return "Policy not found for number: " + policyNumber;
        }
        Policy p = found.get();

        StringBuilder result = new StringBuilder();
        result.append("Policy Details for ").append(p.getPolicyNumber()).append("\n");
        result.append("------------------------\n");
        result.append("Policy Holder: ").append(p.getPolicyHolder()).append("\n");
        result.append("Coverage Type: ").append(p.getCoverageType()).append("\n");
        result.append("Coverage Amount: $").append(p.getCoverageAmount()).append("\n");
        result.append("Premium: $").append(p.getPremium()).append("\n");
        result.append("Coverage Percentage: ").append(asPercent(p.getCoveragePercentage())).append("\n");
        result.append("Effective Date: ").append(p.getEffectiveDate()).append("\n");
        result.append("Expiry Date: ").append(p.getExpiryDate()).append("\n");
        result.append("Status: ").append(p.getStatus()).append("\n");

        return result.toString();
    }

    private String asPercent(java.math.BigDecimal pct) {
        if (pct == null) {
            return "N/A";
        }
        return pct.multiply(new java.math.BigDecimal("100")).stripTrailingZeros().toPlainString() + "%";
    }
}
