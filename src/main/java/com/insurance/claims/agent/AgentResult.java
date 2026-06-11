package com.insurance.claims.agent;

import java.util.List;

/**
 * Result of an agent run: the final natural-language reply plus a trace of the
 * tool calls the agent made to get there.
 */
public record AgentResult(String reply, List<String> toolTrace) {
}
