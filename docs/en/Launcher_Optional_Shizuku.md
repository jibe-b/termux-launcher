# Optional Shizuku Integration

Shizuku is not required for normal launcher usage.

When enabled and granted, Shizuku supports optional privileged launcher features:

- Double-tap the alphabet row to lock the phone.
- System status helpers for tmux/status bar integrations.
- Controlled privileged command execution through LauncherCtl policy, when explicitly enabled.

## Setup

1. Install and start [Shizuku](https://github.com/rikkaapps/shizuku).
2. Open Termux Launcher.
3. Grant the launcher Shizuku permission when prompted.
4. Check status:

```sh
launcherctl status
```

## LauncherCtl Exec Policy

Privileged command execution is disabled by default. If you enable it, the policy is stored at:

```sh
~/.launcherctl/config.json
```

Allowed commands must match configured prefixes. See [LauncherCtl API](LauncherCtl_API) for the policy format and security model.

## Privacy Notes

Notification and media helpers require Android notification listener access. Without that permission, the launcher still works normally, but `launcherctl media`, `launcherctl art`, and `launcherctl notifications` return limited or empty data.
