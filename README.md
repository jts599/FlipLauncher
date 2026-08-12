# FlipLauncher

FlipLauncher is an Android Home application whose initial screen recreates the flip-phone launcher mockup in `index.html`. Its screen actions, quick-action bars, and keypad are intentionally visual only; their behavior will be added separately.

## Build

```bash
./gradlew assembleDebug
```

Install the resulting APK, then use Android Settings to select **FlipLauncher** as the default Home app.

## Development container

Open the repository with VS Code's **Dev Containers: Reopen in Container** command. The included container supplies Java 21, Android SDK platform 35/build tools 35.0.0, ADB, Node.js, and Codex.
