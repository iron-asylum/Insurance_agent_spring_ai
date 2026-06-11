package com.insurance.claims.tool;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class NotificationTool implements AgentTool {

    @Override
    public String name() {
        return "send_notification";
    }

    @Override
    public String description() {
        return "Send a notification to a claimant about a claim status update. Use channel 'email' for an email or 'sms' for a short message.";
    }

    @Override
    public List<ToolParam> parameters() {
        return List.of(
                new ToolParam("claimId", "The claim id, e.g. CLM001", true),
                new ToolParam("status", "The new claim status, e.g. APPROVED", true),
                new ToolParam("recipient", "Recipient: an email address for email channel, or a name/phone for sms", true),
                new ToolParam("channel", "Delivery channel: 'email' or 'sms' (defaults to sms)", false)
        );
    }

    @Override
    public String execute(Map<String, String> args) {
        String channel = args.getOrDefault("channel", "sms");
        if ("email".equalsIgnoreCase(channel)) {
            return sendEmailNotification(args.get("claimId"), args.get("status"), args.get("recipient"));
        }
        return sendNotification(args.get("claimId"), args.get("status"), args.get("recipient"));
    }

    public String sendNotification(String claimId, String status, String recipient) {
        // Simulate sending notification
        StringBuilder result = new StringBuilder();
        result.append("Notification Sent\n");
        result.append("================\n");
        result.append("To: ").append(recipient).append("\n");
        result.append("Claim ID: ").append(claimId).append("\n");
        result.append("Status: ").append(status).append("\n");
        result.append("Message: Your claim has been ").append(safeLower(status))
                .append(". Please review the details.");

        return result.toString();
    }

    public String sendEmailNotification(String claimId, String status, String email) {
        // Simulate sending email notification
        StringBuilder result = new StringBuilder();
        result.append("Email Notification Sent\n");
        result.append("=======================\n");
        result.append("Recipient: ").append(email).append("\n");
        result.append("Claim ID: ").append(claimId).append("\n");
        result.append("Status: ").append(status).append("\n");
        result.append("Subject: Claim Status Update - ").append(claimId).append("\n");
        result.append("Body: Dear Customer,\n\nYour insurance claim ").append(claimId)
                .append(" has been ").append(safeLower(status))
                .append(".\n\nThank you for your patience.\n\nBest regards,\nInsurance Claims Team");

        return result.toString();
    }

    private String safeLower(String s) {
        return s == null ? "updated" : s.toLowerCase();
    }
}
