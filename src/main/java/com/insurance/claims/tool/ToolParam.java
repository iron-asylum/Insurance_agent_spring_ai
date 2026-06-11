package com.insurance.claims.tool;

/**
 * A single tool parameter declaration used to render the tool signature
 * that is shown to the model in the system prompt.
 */
public record ToolParam(String name, String description, boolean required) {
}
