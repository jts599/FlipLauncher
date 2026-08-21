# FlipLauncher

FlipLauncher is an Android Home application with a keypad-first flip-phone interface. The LCD is display-only: its contextual labels are invoked by the red, yellow, and green action bars. Home exposes Search, Quick Launch, and Settings; dialable keypad presses enter Dialer. Search uses T9 entry, while Quick Launch and Settings remap `2/4/6/8` to arrows and `5` to Select.

## Build

```bash
./gradlew assembleDebug
```

Install the resulting APK, then use Android Settings to select **FlipLauncher** as the default Home app.

## Development container

Open the repository with VS Code's **Dev Containers: Reopen in Container** command. The included container supplies Java 21, Android SDK platform 35/build tools 35.0.0, ADB, Node.js, and Codex.
