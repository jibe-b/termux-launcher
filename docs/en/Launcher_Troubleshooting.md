# Troubleshooting

## Terminal Input or Screen Updates Are Slow

If terminal input or screen updates become unusually slow after an app update, launcher restart, or final shell exit, run:

```sh
termux-reload-settings
```

This recreates the activity styling layer around the existing Termux session and usually clears stale terminal UI state.

## Restart the Launcher

If the launcher itself needs a full restart:

```sh
launcherctl restart
```

## Shell Exited While Launcher Was Home

If the shell exits while the launcher is active as the system home app, Android can leave the process in a degraded state until the launcher is restarted.

Use:

```sh
launcherctl restart
```

If the bridge is unavailable, restart the app from Android.

## Android Phantom Process Limits

Android 12+ phantom process restrictions can still affect long-running Termux workloads under heavy background pressure. See [termux-app issue #2366](https://github.com/termux/termux-app/issues/2366).

## LauncherCtl Is Not Responding

Check:

```sh
launcherctl status
```

If the command is missing or the endpoint cannot be reached, restart the launcher. The app installs the CLI script when `TermuxActivity` starts.

## Shizuku Features Do Not Work

Normal launcher usage does not require Shizuku. For Shizuku-only features:

- Confirm Shizuku is running.
- Grant permission to Termux Launcher.
- Run `launcherctl status` and check backend state.

## Media or Notifications Are Empty

`launcherctl media`, `launcherctl art`, and `launcherctl notifications` require Android notification listener access. Grant that permission in Android settings, then check again.
