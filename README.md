# Termux Launcher

Termux Launcher turns Termux into an Android home screen focused on keyboard-first workflows, terminal-heavy use, and fast app launching.

It is based on [termux-monet](https://github.com/Termux-Monet/termux-monet), with launcher-specific UI built on top of the upstream [termux-app](https://github.com/termux/termux-app) codebase.

Download builds from [Releases](https://github.com/PickleHik3/termux-launcher/releases).

## What It Adds

- Terminal-first Android launcher experience
- App icons row with paging and page indicators
- AZ scrub row for fast application filtering and launching
- Wallpaper-aware theming and visual customization
- Optional terminal blur, grain, and opacity controls
- Pinned apps workflow inside the launcher surface
- Optional monochrome app icons

## Companion Apps

Use the matching forks below to avoid shared UID and signature mismatches:

- [Termux:API](https://github.com/PickleHik3/termux-api)
- [Termux:Styling](https://github.com/PickleHik3/termux-styling)

## Optional Shizuku Integration

Shizuku is not required for normal app launching, filtering, pinned apps, or the launcher UI.

If enabled, the current privileged integrations are limited to:

- lock screen on AZ-row double tap
- system stats support for tmux status bar integrations

If you use the tmux status bar helpers, see [tooie](https://github.com/PickleHik3/tooie) for the companion tooling and setup flow.

## Setup Notes

- Installing [Shizuku](https://github.com/rikkaapps/shizuku) is optional.
- Installing [Unexpected Keyboard](https://github.com/Julow/Unexpected-Keyboard) is recommended for heavy tmux and shell usage.
- By default, typing `/` in the terminal starts app search in the launcher bar. You can change that from `Settings -> Apps Bar -> Input split character`.

## Screenshots

| Home | Apps Bar | Settings |
| --- | --- | --- |
| ![Home](screenshots/home-screen.png) | ![Apps Bar](screenshots/app-search.png) | ![Settings](screenshots/app-settings.png) |

## Known Limitations

- Android 12+ phantom process restrictions can still affect Termux stability under heavy background process load. See the upstream Termux guidance in [termux-app issue #2366](https://github.com/termux/termux-app/issues/2366).
- If the shell is exited while the app is active as the system launcher, Android may leave the app in a degraded state until the launcher process is restarted.

## Upstream Base

- [termux-app](https://github.com/termux/termux-app)
- [termux-monet](https://github.com/Termux-Monet/termux-monet)
- [TEL](https://github.com/t-e-l/tel)
