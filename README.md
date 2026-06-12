# 🛡️ Insurance Claims Agent — Spring AI + Local LLM

An insurance claims management system with an **AI agent that runs entirely on your own machine**.
A Spring Boot backend exposes a claims/policies database to a local LLM (served by
[llama.cpp](https://github.com/ggml-org/llama.cpp)) through five tools, and a single-page web UI
provides a dashboard, data browsers, and a chat interface to the agent.

No cloud APIs, no API keys, no data leaving your machine.

## Features

- **Agent chat** — ask things like *"Look up policy POL123456"* or *"Calculate the payout for a
  $20,000 claim on POL789012"*. The agent plans, calls database-backed tools, and answers with a
  visible tool-call trace.
- **Dashboard** — live stats, claims-by-status donut, claims-per-month chart, coverage-type
  breakdown, recent claims.
- **Claims browser** — search + status filters across all claims; click any row for full details
  including the document analysis on file.
- **Policies browser** — search + filters; click a policy to see its terms and every claim filed
  under it.
- **Self-seeding database** — on first run the app seeds a file-backed H2 database with curated
  demo records plus ~50 policies / ~150 claims of realistic mock data (generated with
  [Datafaker](https://www.datafaker.net/)).
- **MCP server** — the same five tools are also published over the
  [Model Context Protocol](https://modelcontextprotocol.io/), so external clients like Claude
  Desktop, Claude Code, or the MCP Inspector can call them directly. See
  [Use the tools from any MCP client](#use-the-tools-from-any-mcp-client).

## Architecture

```
Browser (SPA)         External MCP client
dashboard/claims/      (Claude Desktop / Code,
policies/chat          MCP Inspector)
        │                     │
        │ HTTP                │ MCP over SSE (/sse)
        ▼                     ▼
Spring Boot app  :8081
  ├── REST API   (/api/claims, /api/policies, /api/agent)
  ├── MCP server (publishes the 5 tools to external clients)
  ├── Agent loop (ClaimAgentService + ToolRegistry)
  │     ├── lookup_policy        ─┐
  │     ├── get_claim_history     │   one shared implementation per tool,
  │     ├── analyze_documents     ├─  used by BOTH the local agent and MCP
  │     ├── calculate_payout      │   → read/write H2 database (./data/claims.mv.db)
  │     └── send_notification    ─┘
  └── Spring AI OpenAI-compatible client
        │
        ▼
llama.cpp server  :8080   (any GGUF chat model, e.g. Qwen)
```

The agent uses Spring AI's OpenAI-compatible client purely as a protocol — it points at
`localhost:8080`, so any OpenAI-compatible server works (llama.cpp, Ollama, LM Studio, vLLM…).
The MCP server (`ClaimMcpTools`) wraps the same tool beans, so the in-process agent and any remote
MCP client run identical code against the same database.

## Tech stack

| Layer     | Choice                                              |
|-----------|-----------------------------------------------------|
| Backend   | Java 17, Spring Boot 3.4.5, Spring AI 1.0.0         |
| Database  | H2 (file mode, persists in `./data/`), Spring Data JPA |
| Mock data | Datafaker 2.4.2                                     |
| LLM       | llama.cpp server with a local GGUF model            |
| MCP       | Spring AI MCP server (WebMVC/SSE transport)         |
| Frontend  | Single static page (vanilla JS + hand-rolled SVG charts), no build step |

## Getting started

### Prerequisites

- **JDK 17** and **Maven** (e.g. `brew install openjdk@17 maven`)
- **llama.cpp** with a tool-calling-capable chat model (Qwen 2.5/3 family works well)

### 1. Start the local LLM

```bash
llama-server -m your-model.gguf --port 8080
```

Then check the model id it reports and put it in `src/main/resources/application.properties`:

```bash
curl http://localhost:8080/v1/models
```

```properties
spring.ai.openai.base-url=http://localhost:8080
spring.ai.openai.chat.options.model=<model id from /v1/models>
```

### 2. Run the app

```bash
mvn spring-boot:run
# or
mvn -DskipTests package && java -jar target/claims-agent-system-1.0.0.jar
```

On first start the database is created and seeded automatically. Then open:

- **App UI:** http://localhost:8081
- **H2 console:** http://localhost:8081/h2-console (JDBC URL `jdbc:h2:file:./data/claims`, user `sa`, empty password)

### Try these in the chat

- `Look up policy POL123456`
- `Show me John Smith's claim history`
- `Analyze the documents for claim CLM003`
- `Calculate the payout for a 20000 dollar claim on POL789012`

## REST API

| Method | Endpoint                          | Description                            |
|--------|-----------------------------------|----------------------------------------|
| POST   | `/api/agent/chat`                 | Chat with the agent (`{"message": "…"}`) |
| GET    | `/api/agent/tools`                | List the agent's registered tools      |
| GET    | `/api/claims`                     | All claims (optional `?status=PAID`)   |
| GET    | `/api/claims/{id}`                | One claim                              |
| GET    | `/api/claims/{id}/document`       | Document analysis for a claim          |
| POST   | `/api/claims`                     | Create a claim                         |
| GET    | `/api/policies`                   | All policies                           |
| GET    | `/api/policies/{number}`          | One policy                             |
| GET    | `/api/policies/{number}/claims`   | Claims filed under a policy            |

## Use the tools from any MCP client

While the app runs, it also exposes the five tools over the **Model Context Protocol** on an SSE
endpoint, so any MCP-aware client can call them — no llama.cpp required for this part.

- **SSE endpoint:** `http://localhost:8081/sse`
- **Tools published:** `lookup_policy`, `get_claim_history`, `analyze_documents`,
  `calculate_payout`, `send_notification`

**Claude Code (CLI):**

```bash
claude mcp add --transport sse insurance-claims http://localhost:8081/sse
```

**Claude Desktop** (or any client that takes a JSON config) — bridge the SSE server with
[`mcp-remote`](https://www.npmjs.com/package/mcp-remote):

```json
{
  "mcpServers": {
    "insurance-claims": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "http://localhost:8081/sse"]
    }
  }
}
```

**Inspect / test it directly** with the official inspector:

```bash
npx @modelcontextprotocol/inspector
# then connect to http://localhost:8081/sse (transport: SSE)
```

## Project structure

```
src/main/java/com/insurance/claims/
├── agent/        # Agent loop, tool registry, result types
├── tool/         # The five database-backed agent tools
├── mcp/          # MCP adapter (ClaimMcpTools) + server registration
├── controller/   # REST controllers (agent, claims, policies)
├── model/        # JPA entities: Policy, Claim, ClaimDocument
├── repository/   # Spring Data repositories
└── config/       # DataSeeder (curated + Datafaker mock data)
src/main/resources/static/index.html   # The whole UI (SPA, no build step)
```

## Notes

- Port **8080** is reserved for llama.cpp, so the app runs on **8081** (see `application.properties`).
- Mock data is self-generated and reproducible (fixed random seed); delete the `data/` folder to
  reseed from scratch.
- Smaller local models can be over-eager with tool calls — the system prompt lives in
  `ClaimAgentService` if you want to tune behavior.
