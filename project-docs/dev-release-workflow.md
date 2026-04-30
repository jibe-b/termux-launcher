# Dev Branch Release Workflow

## Branch Model

- `dev` is the working branch for this release cycle.
- Changes are implemented on `dev`, pushed to GitHub, and validated from GitHub Actions artifacts.
- The public release remains a single cut after the planned hardening and polish passes are complete.

## Validation Path

This repo should assume GitHub-hosted Android builds for day-to-day validation when a local Android SDK/NDK environment is not available.

Primary workflows:

- `.github/workflows/debug_build.yml`
- `.github/workflows/run_tests.yml`

## Change Loop

1. Make a coherent change on `dev`.
2. Push `dev` to GitHub.
3. Run the debug APK workflow.
4. Install the generated debug APK on-device.
5. Run the manual validation checklist below.
6. Repeat until the current pass is stable.

## Manual Validation Checklist

### Core launcher

- App can be selected as the Android Home app.
- Home button returns to the launcher reliably.
- Launcher still behaves sanely when opened from recents.
- Terminal session starts and remains interactive.

### App surface

- Pinned apps render correctly.
- Pinned folders open and launch apps correctly.
- Search returns expected launcher apps.
- A-Z scrub and vertical launch gesture still work.
- Install/uninstall updates refresh the launcher app catalog.

### Shell bridge

- `launcherctl status` returns backend state.
- `launcherctl apps` returns launchable activities.
- `launcherctl launch <query>` launches the intended app.
- `launcherctl restart` restarts the launcher cleanly.

### Optional privileged behavior

- With Shizuku/root absent, the launcher still works normally.
- With Shizuku available but permission missing, failure states remain understandable.
- With Shizuku granted, lock-screen and status integrations still work.

## Release Gate

Do not cut the public release until:

- Pass 1 hardening items are merged into `dev`.
- Pass 2 polish items are merged into `dev`.
- GitHub Actions debug APK builds succeed.
- On-device validation passes against the debug APK artifact.
