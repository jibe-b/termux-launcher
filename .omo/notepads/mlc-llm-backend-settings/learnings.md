## Wave 1 Task 1 - backend-aware schema

- `TaiModelSpec` now has an explicit allowlist for LiteRT (`litert-lm`/`litertlm`) and MLC (`mlc-llm`/`mlc`) backend-format pairs. Unsupported JSON fails deterministically with `unsupported_backend`, `unsupported_format`, or `unsupported_backend_format`.
- Path inference treats `.mlc`, `mlc-ai`, and `mlc-llm` substrings as MLC hints; everything else keeps the existing LiteRT default for backward compatibility.
- Remote catalog and user model storage both use the shared allowlist, so LiteRT entries remain unchanged while MLC records are preserved instead of filtered out.

## Wave 1 Task 6 - opt-in LAN API binding

- `TaiSettings` persists `tai_api_bind_mode` with a safe `localhost` default and normalizes every invalid value back to `localhost`; only `lan` opts into network exposure.
- `LauncherCtlApiServer` keeps bearer-token auth shared across both modes, binds localhost mode to `127.0.0.1`, binds LAN mode to `0.0.0.0`, and emits no CORS headers.
- Endpoint/settings JSON exposes `bindMode`; LAN warning text is present only when `bindMode` is `lan`.
- Local Java LSP diagnostics were unavailable because `jdtls` is not installed in this environment; use Gradle unit tests and GitHub Actions `Build nightly` as the verification gate for this task.
