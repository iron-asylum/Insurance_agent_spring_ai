package com.insurance.claims.mcp;

import com.insurance.claims.tool.ClaimHistoryTool;
import com.insurance.claims.tool.DocumentAnalysisTool;
import com.insurance.claims.tool.NotificationTool;
import com.insurance.claims.tool.PayoutCalculatorTool;
import com.insurance.claims.tool.PolicyLookupTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * Exposes the same insurance-claims business logic the local agent uses as
 * Model Context Protocol (MCP) tools, so external MCP clients (Claude Desktop,
 * Claude Code, other agents) can call them over the server's SSE endpoint.
 *
 * Each method delegates to the existing {@code AgentTool} bean, so there is a
 * single source of truth per operation: the in-process llama.cpp agent and any
 * remote MCP client run exactly the same code against the same H2 database.
 */
@Service
public class ClaimMcpTools {

    private final PolicyLookupTool policyLookupTool;
    private final ClaimHistoryTool claimHistoryTool;
    private final DocumentAnalysisTool documentAnalysisTool;
    private final PayoutCalculatorTool payoutCalculatorTool;
    private final NotificationTool notificationTool;

    public ClaimMcpTools(PolicyLookupTool policyLookupTool, ClaimHistoryTool claimHistoryTool,
                         DocumentAnalysisTool documentAnalysisTool, PayoutCalculatorTool payoutCalculatorTool,
                         NotificationTool notificationTool) {
        this.policyLookupTool = policyLookupTool;
        this.claimHistoryTool = claimHistoryTool;
        this.documentAnalysisTool = documentAnalysisTool;
        this.payoutCalculatorTool = payoutCalculatorTool;
        this.notificationTool = notificationTool;
    }

    @Tool(name = "lookup_policy",
            description = "Look up policy details (holder, coverage type, coverage amount, premium, dates, status) by policy number.")
    public String lookupPolicy(
            @ToolParam(description = "The policy number, e.g. POL123456") String policyNumber) {
        return policyLookupTool.lookupPolicy(policyNumber);
    }

    @Tool(name = "get_claim_history",
            description = "Get the past claim history (id, policy number, description, amount, status, date) for a policy holder by full name.")
    public String getClaimHistory(
            @ToolParam(description = "Full name of the policy holder, e.g. John Smith") String policyHolderName) {
        return claimHistoryTool.getClaimHistory(policyHolderName);
    }

    @Tool(name = "analyze_documents",
            description = "Analyze the supporting documents for a claim and return summary, key findings, evidence type, relevance, confidence and risk.")
    public String analyzeDocuments(
            @ToolParam(description = "The claim reference to analyze, e.g. CLM001") String claimId) {
        return documentAnalysisTool.analyzeDocuments(claimId);
    }

    @Tool(name = "calculate_payout",
            description = "Calculate the estimated claim payout from the policy's coverage percentage and the claim amount.")
    public String calculatePayout(
            @ToolParam(description = "The policy number, e.g. POL123456") String policyNumber,
            @ToolParam(description = "The claim amount as a plain number, e.g. 15000") String claimAmount) {
        return payoutCalculatorTool.calculatePayout(policyNumber, claimAmount);
    }

    @Tool(name = "send_notification",
            description = "Send a (simulated) notification to a claimant about a claim status update via email or sms.")
    public String sendNotification(
            @ToolParam(description = "The claim id, e.g. CLM001") String claimId,
            @ToolParam(description = "The new claim status, e.g. APPROVED") String status,
            @ToolParam(description = "Recipient: an email address for the email channel, or a name/phone for sms") String recipient,
            @ToolParam(description = "Delivery channel: 'email' or 'sms' (defaults to sms)", required = false) String channel) {
        if ("email".equalsIgnoreCase(channel)) {
            return notificationTool.sendEmailNotification(claimId, status, recipient);
        }
        return notificationTool.sendNotification(claimId, status, recipient);
    }
}
