## Wave 1 Task 1 - backend-aware schema

- `TaiModelSpec` now has an explicit allowlist for LiteRT (`litert-lm`/`litertlm`) and MLC (`mlc-llm`/`mlc`) backend-format pairs. Unsupported JSON fails deterministically with `unsupported_backend`, `unsupported_format`, or `unsupported_backend_format`.
- Path inference treats `.mlc`, `mlc-ai`, and `mlc-llm` substrings as MLC hints; everything else keeps the existing LiteRT default for backward compatibility.
- Remote catalog and user model storage both use the shared allowlist, so LiteRT entries remain unchanged while MLC records are preserved instead of filtered out.

## Wave 1 Task 6 - opt-in LAN API binding

- `TaiSettings` persists `tai_api_bind_mode` with a safe `localhost` default and normalizes every invalid value back to `localhost`; only `lan` opts into network exposure.
- `LauncherCtlApiServer` keeps bearer-token auth shared across both modes, binds localhost mode to `127.0.0.1`, binds LAN mode to `0.0.0.0`, and emits no CORS headers.
- Endpoint/settings JSON exposes `bindMode`; LAN warning text is present only when `bindMode` is `lan`.
- Local Java LSP diagnostics were unavailable because `jdtls` is not installed in this environment; use Gradle unit tests and GitHub Actions `Build nightly` as the verification gate for this task.

## Wave 2 Task 2 - LiteRT/MLC runtime routing

- `MultiBackendTaiRuntime` remains the only backend switch point: `BACKEND_MLC_LLM` routes to `MlcTaiRuntime`, everything else routes to `DualSlotTaiRuntime`, and `MobileActions-270M` still short-circuits to LiteRT.
- The active-generation guard still runs before switching assistant backends and returns 409 `generation_active` without unloading the current runtime.
- `MlcTaiRuntime` is intentionally a no-inference stub for Task 2: load returns 501 `mlc_runtime_unavailable`, generation APIs return 501 `unsupported_operation`, and Task 3 should replace only this adapter internals with real bundled-artifact loading.

## Wave 2 Task 2 - Verification

- GitHub Actions run 27653974561 passed successfully for commit 8ebcc0fe
- Unit tests passed including MultiBackendTaiRuntimeTest
- Artifacts downloaded: arm64-v8a APK, sha256sums, universal, x86, x86_64, armeabi-v7a
- Task 2 acceptance criteria all met:
  - LiteRT models route to DualSlotTaiRuntime
  - MLC models route to MlcTaiRuntime
  - Active-generation backend switch returns 409 generation_active
  - Mobile-actions companion remains LiteRT-only

## Wave 2 Task 3 - MLC runtime trust boundary

- Replaced `MlcTaiRuntime` stub with a real adapter that mirrors the `LiteRtTaiRuntime` load/keep-warm/unload/cancel state contract.
- `MlcBundledLibraryRegistry` maps `modelLibraryId` → ABI / native library names / capabilities / SHA-256 hash. Only arm64-v8a is registered; other ABIs return 501 `mlc_runtime_unavailable`.
- `TaiMlcPackageValidator` enforces the trust boundary: rejects custom-downloaded `.so` files, raw weights (`.safetensors`, `.gguf`, `.bin`, `.pt`, `.onnx`), path traversal (`../`), and verifies SHA-256 hashes.
- `MlcTaiRuntime.load()` performs five guarded steps:
  1. Device ABI vs registry check
  2. `modelLibraryId` registry lookup
  3. Package path validation via `TaiMlcPackageValidator`
  4. Existence check of every listed native library in `context.getApplicationInfo().nativeLibraryDir`
  5. State transition to `loaded` (native initialization is stubbed because no published MLC AAR exists yet)
- `MlcTaiRuntime` uses explicit `synchronized` blocks rather than method-level `synchronized` so that `cancel()` can race and set `loadCancellationRequested` during the stubbed init window, matching the `LiteRtTaiRuntime` concurrency pattern.
- `MultiBackendTaiRuntime` now passes `Context` to `MlcTaiRuntime`; no changes to routing logic.
- No build.gradle changes were required because no published MLC Android AAR is available. The expected integration path (clone MLC repo, run `mlc_llm package`, copy `.so` files to `jniLibs/<abi>/`, update registry hashes) is documented in `MlcBundledLibraryRegistry` JavaDoc.
- `MlcTaiRuntimeTest` covers unsupported ABI, missing bundled library, custom `.so` rejection, valid load/unload state transitions, keep-warm → `idle-warm`, cancellation during load, generation APIs returning 501/409, and path-traversal rejection. Tests use fakes (temp dirs and ABI overrides) and do not require a real MLC native runtime.
- Local Java/Android build environment is unavailable in this workspace; verification is delegated to GitHub Actions `Build nightly` on the `experimental` branch.

## Wave 2 Task 4 - MLC device capability detection and disabled-with-reason model gating

- Extended `TaiDeviceCapabilities` with MLC backend support fields: `mlcSupported`, `mlcUnsupportedReason`, `mlcSdkMinimum` (24), `mlcMemoryEstimateMb` (2048), `mlcAcceleratorInfo`, and `mlcBundledLibrariesAvailable`.
- `mlcSupported` is derived from device ABI vs `MlcBundledLibraryRegistry` and SDK minimum; unsupported devices get a concrete human-readable reason string instead of silent disabling.
- `toJson()` now surfaces a `backends` object with both `litert-lm` and `mlc-llm` booleans, plus `mlcUnsupportedReason` when MLC is unavailable, allowing settings/UI to disable MLC controls before download/load.
- Added `checkModelCapability(TaiModelSpec)` which returns a `ModelCapabilityCheck` with optional warning (memory) and blocking reason (MLC backend unsupported), keeping per-model gating logic centralized in device capabilities.
- Added debug-build-only capability override (`setDebugMlcUnsupportedReason`) gated by `BuildConfig.DEBUG`. QA can force `mlcUnsupportedReason` for settings/UI tests without needing multiple physical devices; completely ignored in release builds.
- `TaiDeviceCapabilitiesTest` covers: supported arm64-v8a device, unsupported ABI with concrete reason, low SDK reason, debug override in debug builds, release-build override ignore via `shouldApplyDebugOverride`, MLC model blocking on unsupported device, LiteRT model memory warning, and correct JSON backends structure.
- `createForTest()` package-private factory allows tests to inject fake ABIs without mocking `Build.SUPPORTED_ABIS` directly.
