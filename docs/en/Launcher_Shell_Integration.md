# Shell Integration

`launcherctl` is the local shell bridge installed by the launcher. It talks to a localhost API running inside the app process and uses the same app catalog as the on-screen launcher.

## Launch Apps

```sh
launcherctl launch whatsapp
```

List launchable apps:

```sh
launcherctl apps
```

Check bridge state:

```sh
launcherctl status
```

Restart the launcher:

```sh
launcherctl restart
```

## tmux Binding Example

```tmux
bind -n M-w run-shell 'tmux display-message "Opening WhatsApp"; launcherctl launch whatsapp >/dev/null 2>&1 || tmux display-message "Launch failed: WhatsApp"'
```

## System and Media Data

LauncherCtl can expose system resources, notification state, media metadata, and album art when the required Android permissions are granted.

Common commands:

```sh
launcherctl resources
launcherctl media
launcherctl art
launcherctl notifications
```

For endpoint details and security behavior, see [LauncherCtl API](LauncherCtl_API).
