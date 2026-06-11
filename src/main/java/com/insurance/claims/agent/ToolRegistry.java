package com.insurance.claims.agent;

import com.insurance.claims.tool.AgentTool;
import com.insurance.claims.tool.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Discovers every {@link AgentTool} bean and exposes them by name, plus renders
 * the tool catalog that is injected into the agent's system prompt.
 */
@Component
public class ToolRegistry {

    private final Map<String, AgentTool> tools = new LinkedHashMap<>();

    public ToolRegistry(List<AgentTool> toolBeans) {
        for (AgentTool tool : toolBeans) {
            tools.put(tool.name(), tool);
        }
    }

    public Collection<AgentTool> all() {
        return tools.values();
    }

    public AgentTool get(String name) {
        return tools.get(name);
    }

    public boolean has(String name) {
        return tools.containsKey(name);
    }

    /** Renders each tool as `name(param1, param2) - description` with a parameter glossary. */
    public String describeForPrompt() {
        StringBuilder sb = new StringBuilder();
        for (AgentTool tool : tools.values()) {
            StringJoiner sig = new StringJoiner(", ", tool.name() + "(", ")");
            for (ToolParam p : tool.parameters()) {
                sig.add(p.required() ? p.name() : p.name() + "?");
            }
            sb.append("- ").append(sig).append(": ").append(tool.description()).append("\n");
            for (ToolParam p : tool.parameters()) {
                sb.append("    * ").append(p.name())
                        .append(p.required() ? " (required): " : " (optional): ")
                        .append(p.description()).append("\n");
            }
        }
        return sb.toString();
    }
}
