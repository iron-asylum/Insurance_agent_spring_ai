package com.insurance.claims.tool;

import java.util.List;
import java.util.Map;

/**
 * Contract every agent-callable tool implements. The agent advertises these
 * tools to the LLM via the system prompt and dispatches calls by {@link #name()}.
 */
public interface AgentTool {

    /** Unique, snake_case tool name the model uses to call this tool. */
    String name();

    /** Human/LLM-readable description of what the tool does. */
    String description();

    /** Declared parameters, used to build the tool's signature in the prompt. */
    List<ToolParam> parameters();

    /** Execute the tool with the model-supplied arguments and return a text result. */
    String execute(Map<String, String> args);
}
