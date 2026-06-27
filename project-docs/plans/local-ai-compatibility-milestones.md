# Local AI compatibility milestones

The goal is a frictionless, capability-aware local model service backed by the existing
LiteRT-LM and MNN runtimes. OpenAI and Ollama are transport adapters over one internal model and
generation contract; neither protocol owns backend behavior or model metadata.

## Milestone 1: protocol-neutral generation contract

- [ ] Rename protocol-specific fields in `TaiChatRequest` to neutral message, tool, and tool-choice fields.
- [ ] Preserve temporary deprecated aliases so the runtime refactor remains source compatible.
- [ ] Route both LiteRT-LM and MNN through the neutral fields.
- [ ] Keep text, multimodal content, tool calls, streaming, cancellation, and runtime options in the shared contract.
- [ ] Add regression tests proving protocol adapters do not change backend request semantics.

## Milestone 2: authoritative model registry

- [ ] Keep `TaiModelSpec` as the canonical source for backend, format, endpoint capabilities, source capabilities, limits, and tool mode.
- [ ] Add protocol-neutral discovery metadata needed by OpenAI and Ollama adapters.
- [ ] Ensure endpoint-facing capability metadata always describes runnable behavior.
- [ ] Ensure installed/readable models are the only models published by generation APIs.
- [ ] Add schema and discovery tests for chat, vision, audio, tools, and embeddings.

## Milestone 3: hardened LiteRT-LM and MNN imports

- [ ] Validate backend/package combinations before registration.
- [ ] Infer embedding packages conservatively instead of defaulting every unknown `.tflite` to chat.
- [ ] Preserve explicitly declared capabilities and mark inferred capability provenance.
- [ ] Keep MNN package-sidecar validation and LiteRT package validation as backend-owned checks.
- [ ] Publish a newly imported readable model through the shared registry without client-specific configuration.
- [ ] Add import tests for chat, embeddings, unsupported raw weights, and explicit capability overrides.

## Milestone 4: OpenAI and Ollama compatibility APIs

- [ ] Normalize request paths separately from query strings.
- [ ] Keep `/v1/models`, `/v1/chat/completions`, `/v1/completions`, and `/v1/embeddings` compatible.
- [ ] Add stateless `/v1/responses`, including text, images, function tools, function results, and streaming events.
- [ ] Add Ollama `/api/version`, `/api/tags`, `/api/show`, `/api/chat`, `/api/generate`, `/api/ps`, and `/api/embed`.
- [ ] Use NDJSON for native Ollama streaming and SSE for OpenAI streaming.
- [ ] Return explicit unsupported-operation errors for Ollama registry operations that cannot map to LiteRT-LM/MNN packages.
- [ ] Add endpoint contract tests and real-client smoke fixtures.

## Milestone 5: frictionless client setup

- [ ] Add capability-aware configuration generation for AIChat, OpenCode, Crush, Codex, and Ollama clients.
- [ ] Read endpoint and token from the existing `~/.launcherctl` contract without embedding secrets in tracked files.
- [ ] Generate ordinary chat configurations for text models and agent configurations only for tool-capable models.
- [ ] Document loopback authentication and the optional standard Ollama port/security tradeoff.
- [ ] Surface supported endpoints and compatibility status through launcher settings and `launcherctl`.
- [ ] Add shell-level tests for generated configurations.

## Release verification

- [ ] Run focused JVM/unit and shell tests available without an Android SDK.
- [ ] Run `git diff --check` and inspect the final diff.
- [ ] Commit and push to `experimental`.
- [ ] Verify the GitHub Actions debug APK workflow and report the run URL/result.
