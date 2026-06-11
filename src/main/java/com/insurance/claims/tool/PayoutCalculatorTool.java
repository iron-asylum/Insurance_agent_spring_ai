package com.insurance.claims.tool;

import com.insurance.claims.model.Policy;
import com.insurance.claims.repository.PolicyRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PayoutCalculatorTool implements AgentTool {

    private final PolicyRepository policyRepository;

    public PayoutCalculatorTool(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    public String name() {
        return "calculate_payout";
    }

    @Override
    public String description() {
        return "Calculate the estimated claim payout based on the policy's coverage percentage and the claim amount.";
    }

    @Override
    public List<ToolParam> parameters() {
        return List.of(
                new ToolParam("policyNumber", "The policy number, e.g. POL123456", true),
                new ToolParam("claimAmount", "The claim amount as a plain number, e.g. 15000", true)
        );
    }

    @Override
    public String execute(Map<String, String> args) {
        return calculatePayout(args.get("policyNumber"), args.get("claimAmount"));
    }

    public String calculatePayout(String policyNumber, String claimAmount) {
        if (policyNumber == null || policyNumber.isBlank()) {
            return "Error: policyNumber is required.";
        }
        if (claimAmount == null || claimAmount.isBlank()) {
            return "Error: claimAmount is required.";
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(claimAmount.trim());
        } catch (NumberFormatException e) {
            return "Error: claimAmount must be a number, got: " + claimAmount;
        }

        Optional<Policy> found = policyRepository.findByPolicyNumber(policyNumber.trim());
        if (found.isEmpty()) {
            return "Policy not found for number: " + policyNumber + ". Cannot calculate payout.";
        }
        Policy policy = found.get();
        BigDecimal coveragePercentage = policy.getCoveragePercentage() != null
                ? policy.getCoveragePercentage()
                : new BigDecimal("0.95");

        BigDecimal payout = amount.multiply(coveragePercentage).setScale(2, RoundingMode.HALF_UP);
        int coveragePct = coveragePercentage.multiply(new BigDecimal("100")).intValue();

        StringBuilder result = new StringBuilder();
        result.append("Payout Calculation for Policy ").append(policy.getPolicyNumber()).append("\n");
        result.append("==================================\n");
        result.append("Coverage Type: ").append(policy.getCoverageType()).append("\n");
        result.append("Claim Amount: $").append(amount.setScale(2, RoundingMode.HALF_UP)).append("\n");
        result.append("Coverage Percentage: ").append(coveragePct).append("%\n");
        result.append("Deductible Applied: $0\n");
        result.append("Net Payout: $").append(payout).append("\n");

        return result.toString();
    }
}
