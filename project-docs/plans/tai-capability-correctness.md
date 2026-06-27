# TAI capability correctness milestones

This checklist tracks the June 2026 model-catalog and compatibility audit. Endpoint truth is the acceptance criterion: clients must only see capabilities the installed APK can execute.

## 1. Capability provenance and catalog authority

- [x] Keep curated catalog capabilities authoritative for known model IDs.
- [x] Prevent stale download records from replacing curated metadata.
- [x] Mark capabilities verified only when they came from a curated catalog entry.
- [x] Validate signed remote endpoint capabilities against backend/runtime support.

## 2. Protocol endpoint correctness

- [x] Stop advertising MNN embeddings until the runtime implements them.
- [x] Gate chat and completion requests on `text_chat`.
- [x] Preserve Responses API `input_audio` content.
- [x] Report Ollama `completion` only for generation-capable models.
- [x] Keep OpenAI, Responses, and Ollama capability behavior covered by tests.

## 3. Coding-client discovery

- [x] Expose Codex discovery records only for tool-capable models with at least a 16K endpoint context.
- [x] Use model-specific coding instructions and modality metadata.
- [x] Keep general chat, embedding, and short-context tool models out of Codex discovery.

## 4. Import declarations and heuristics

- [x] Split vision and audio declarations.
- [x] Add tool-use, code, reasoning, and multilingual declarations.
- [x] Infer capabilities conservatively from model names.
- [x] Restrict raw `.tflite` imports to embeddings.
- [x] Let backend filtering remove unsupported declarations from endpoint metadata.

## 5. Curated metadata and release media

- [x] Correct Gemma 4 E4B, FunctionGemma, Qwen, and MNN catalog metadata.
- [x] Refresh repository screenshots from `Screenshot1.png` through `Screenshot4.png`.
- [x] Refresh the repository demo from `output.gif`.
- [ ] Verify the APK produced by GitHub Actions on `experimental`.
