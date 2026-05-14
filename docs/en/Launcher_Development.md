# Development and Release Workflow

Development happens on the `dev` branch first.

This repo should assume GitHub-hosted Android builds for day-to-day validation when a local Android SDK/NDK environment is not available.

## Main Commands

From the repo root:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest :terminal-emulator:test
./gradlew :app:verifyReleaseHardening
./gradlew :app:connectedDebugAndroidTest
```

## Change Loop

1. Make a coherent change on `dev`.
2. Push `dev` to GitHub.
3. Run the debug APK workflow.
4. Install the generated debug APK on-device.
5. Validate launcher behavior on-device.
6. Repeat until the current pass is stable.

## Manual Validation

- App can be selected as the Android Home app.
- Home button returns to the launcher reliably.
- Launcher still behaves sanely when opened from recents.
- Terminal session starts and remains interactive.
- Pinned apps, folders, search, A-Z scrub, and vertical launch gesture work.
- Install/uninstall updates refresh the launcher app catalog.
- `launcherctl status`, `launcherctl apps`, `launcherctl launch <query>`, and `launcherctl restart` work.
- With Shizuku/root absent, the launcher still works normally.
- With Shizuku granted, lock-screen and status integrations work.

The detailed working checklist lives in [project-docs/dev-release-workflow.md](../../project-docs/dev-release-workflow.md).
