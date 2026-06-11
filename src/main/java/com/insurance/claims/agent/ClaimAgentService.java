package com.insurance.claims.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.claims.tool.AgentTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A prompt-based ReAct agent. It advertises the available {@link AgentTool}s in
 * the system prompt, asks the model to emit a single JSON action each turn, then
 * either dispatches a tool call (feeding the observation back) or returns the
 * final answer. This intentionally does NOT rely on native OpenAI function
 * calling, so it works against a llama.cpp server without the --jinja flag.
 */
@Service
public class ClaimAgentService {

    private static final Logger log = LoggerFactory.getLogger(ClaimAgentService.class);
    private static final int MAX_ITERATIONS = 6;

    private final ChatModel chatModel;
    private final ToolRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    public ClaimAgentService(ChatModel chatModel, ToolRegistry registry) {
        this.chatModel = chatModel;
        this.registry = registry;
    }

    public AgentResult chat(String userMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(buildSystemPrompt()));
        messages.add(new UserMessage(userMessage));

        List<String> trace = new ArrayList<>();

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            String raw = callModel(messages);
            log.debug("Agent iteration {} raw output: {}", i, raw);

            JsonNode action = parseAction(raw);
            if (action == null) {
                // Model didn't emit valid JSON - treat the text as the final answer.
                return new AgentResult(stripJsonFences(raw), trace);
            }

            String type = action.path("action").asText("");

            // Final answer: explicit action, or simply the presence of an "answer" field.
            if ("final".equals(type) || action.has("answer")) {
                return new AgentResult(action.path("answer").asText(raw), trace);
            }

            // Resolve the tool name leniently. Small models often drift from the
            // {"action":"tool","tool":"X"} shape to {"action":"X"} or just {"tool":"X"},
            // so accept a known tool name in either field.
            String toolName = firstKnownTool(action.path("tool").asText(""), type);

            boolean looksLikeToolCall = "tool".equals(type) || action.has("tool") || toolName != null;
            if (looksLikeToolCall) {
                String requested = toolName != null ? toolName
                        : (!action.path("tool").asText("").isEmpty() ? action.path("tool").asText("") : type);
                Map<String, String> args = readArgs(action.get("args"));
                String observation = invokeTool(requested, args);
                trace.add(requested + "(" + args + ")");

                // Record the model's action and the resulting observation, then loop.
                messages.add(new AssistantMessage(raw));
                messages.add(new UserMessage("Observation from " + requested + ":\n" + observation
                        + "\n\nContinue. Respond with the next JSON action."));
                continue;
            }

            // Unknown action shape - surface the raw text rather than looping blindly.
            return new AgentResult(stripJsonFences(raw), trace);
        }

        return new AgentResult(
                "I wasn't able to complete this request within the allowed number of steps. "
                        + "Tools used so far: " + trace,
                trace);
    }

    private String callModel(List<Message> messages) {
        Prompt prompt = new Prompt(messages);
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    private String invokeTool(String toolName, Map<String, String> args) {
        AgentTool tool = registry.get(toolName);
        if (tool == null) {
            return "Error: unknown tool '" + toolName + "'. Available tools: " + registry.all().stream()
                    .map(AgentTool::name).toList();
        }
        try {
            return tool.execute(args);
        } catch (Exception e) {
            log.warn("Tool {} threw", toolName, e);
            return "Error executing " + toolName + ": " + e.getMessage();
        }
    }

    /** Returns the first candidate that names a registered tool, or null if none do. */
    private String firstKnownTool(String... candidates) {
        for (String c : candidates) {
            if (c != null && registry.has(c)) {
                return c;
            }
        }
        return null;
    }

    private Map<String, String> readArgs(JsonNode argsNode) {
        Map<String, String> args = new HashMap<>();
        if (argsNode != null && argsNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = argsNode.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                args.put(e.getKey(), e.getValue().asText());
            }
        }
        return args;
    }

    /** Extracts and parses the first balanced JSON object found in the model output. */
    private JsonNode parseAction(String text) {
        String json = extractJsonObject(text);
        if (json == null) {
            return null;
        }
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractJsonObject(String s) {
        if (s == null) {
            return null;
        }
        int start = s.indexOf('{');
        if (start < 0) {
            return null;
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                } else if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return s.substring(start, i + 1);
                    }
                }
            }
        }
        return null;
    }

    private String stripJsonFences(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("(?s)```(?:json)?", "").trim();
    }

    private String buildSystemPrompt() {
        return """
                You are an insurance claims assistant. You help users by reasoning step by step and
                calling tools to fetch real data instead of guessing.

                AVAILABLE TOOLS:
                %s
                PROTOCOL:
                You must reply with EXACTLY ONE JSON object and nothing else (no markdown, no prose).

                To call a tool:
                {"action": "tool", "tool": "<tool_name>", "args": {"<param>": "<value>"}}

                When you have enough information to answer the user:
                {"action": "final", "answer": "<your complete answer to the user>"}

                RULES:
                - Output strictly one JSON object. Do not wrap it in code fences.
                - Only call tools from the list above, using the exact parameter names shown.
                - After each tool call you will receive an "Observation". Use it to decide the next step.
                - Call tools one at a time. Do not invent data the tools did not return.
                - When done, always finish with an {"action": "final", ...} object.
                """.formatted(registry.describeForPrompt());
    }
}
