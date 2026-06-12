package com.insurance.claims.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the {@link ClaimMcpTools} methods as MCP tool callbacks. The Spring
 * AI MCP server auto-configuration picks up this {@link ToolCallbackProvider}
 * and publishes the tools over the SSE transport (see application.properties).
 */
@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider claimMcpToolCallbacks(ClaimMcpTools claimMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(claimMcpTools)
                .build();
    }
}
