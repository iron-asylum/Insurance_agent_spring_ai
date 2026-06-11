package com.insurance.claims.controller;

import com.insurance.claims.agent.AgentResult;
import com.insurance.claims.agent.ClaimAgentService;
import com.insurance.claims.agent.ToolRegistry;
import com.insurance.claims.tool.AgentTool;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final ClaimAgentService agentService;
    private final ToolRegistry registry;

    public AgentController(ClaimAgentService agentService, ToolRegistry registry) {
        this.agentService = agentService;
        this.registry = registry;
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentResult> chat(@RequestBody ChatRequest request) {
        AgentResult result = agentService.chat(request.message());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tools")
    public ResponseEntity<List<ToolInfo>> tools() {
        List<ToolInfo> infos = registry.all().stream()
                .map(t -> new ToolInfo(t.name(), t.description()))
                .toList();
        return ResponseEntity.ok(infos);
    }

    public record ChatRequest(@NotBlank String message) {
    }

    public record ToolInfo(String name, String description) {
    }
}
