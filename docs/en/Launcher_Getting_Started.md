# Getting Started

## Install & Setup

1. Download and install the latest APK from [Releases](https://github.com/PickleHik3/termux-launcher/releases).

2. It is suggested that you open the app normally at the beginning to finish Termux's bootstrap process.

   - If you want to change the package manager to pacman, you must do it without setting Termux Launcher as the Home app so that you can still get into fail-safe mode. See [Switching package manager](https://wiki.termux.com/wiki/Switching_package_manager).
   - Switching to pacman has benefits, such as having all repositories preconfigured for you, including glibc, tur, etc.
   - Speed-wise, there is no meaningful difference between pacman and the default pkg/apt setup, provided you have chosen a single fastest mirror for the default package manager.

3. Open Android settings and select Termux Launcher as the default Home app. There is a shortcut available for you in Termux Launcher:

   ```text
   Long press on Terminal & more -> Apps Bar -> Set as home launcher
   ```

4. That's it to get you started. Everything works as it would in native Termux.

## Recommended Apps

- [Unexpected Keyboard](https://github.com/Julow/Unexpected-Keyboard) is recommended for terminal and tmux-heavy use.
- [Shizuku](https://github.com/rikkaapps/shizuku) is optional. Install it only if you want the optional privileged features.

Use these matching companion forks if you install Termux add-ons:

- [Termux:API](https://github.com/PickleHik3/termux-api)
- [Termux:Styling](https://github.com/PickleHik3/termux-styling)

Using mismatched Termux add-ons can cause shared UID or signing problems.

## Notes on App Preferences

You can long press on the terminal and click **More** to access relevant preference pages. A few noteworthy ones are mentioned below.

### 1. Appearance

Control the opacity and blur of the terminal, dock, and Termux sessions menu.

- **Terminal Material colors toggle:** Turn this on if you want the Termux shell to inherit the Material colors from the system. It will apply the color scheme to the terminal background, text, cursor, and ANSI colors. Turning this on will create `material-colors.properties` and `material-colors.sh` inside the `~/.termux` directory, which you can source for your specific needs. More information is available at [Launcher Material Colors](https://github.com/PickleHik3/termux-launcher/blob/main/docs/en/Launcher_Material_Colors.md).

- **Dock Blur:** Dock blur does not work if you are using a live wallpaper. It will be automatically disabled if a live wallpaper is detected.

- **Dock Size:** Controls the height of the app icons row.

- **Compact dock spacing:** Tightens the distance between various rows in the dock, including Extra Keys, the A–Z row, and app icons. It also uses a smaller page indicator. It is recommended to turn this on if you want two rows of Termux Extra Keys; otherwise, the terminal size becomes too small. You can find a few examples at [Termux Extra Keys](https://github.com/PickleHik3/termux-launcher/blob/main/docs/en/Termux_Extrakeys.md).

### 2. Apps Bar

All launcher-specific settings are configured here.

- **Double tap alphabets row lockscreen:** You have two options: Shizuku, which provides the system screen-off animation, and Accessibility service, which is the typical method used by other launchers but may cause the screen to flicker once.

- **Search Strictness:** This is something inherited from TEL. I haven't seen a use for it yet.

- **Input Split Character:** The default is `%`. Typing the character specified here will trigger app search, and the results will be displayed on the Apps Bar. It is recommended to choose a seldom-used character.

- **Reset App Order:** By default, app icons are ranked based on the number of times each app has been launched. This was done to make it easier to launch apps with a swipe gesture from the A–Z row to the app icons. The most launched app icon will be directly above its respective alphabet, so you can swipe up to choose and launch it. This button resets the ranking.
