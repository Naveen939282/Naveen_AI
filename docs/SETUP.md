# NAVEEN AI Setup Guide

## Prerequisites

- Android Studio
- Android SDK Platform 36
- Android SDK Build Tools 36.0.0
- JDK 17 or newer
- VS Code
- Git

## Workspace configuration

1. Open the project in VS Code.
2. Confirm Java is available in the environment.
3. Ensure the Android SDK path is configured.
4. Confirm the emulator or device is available for testing.

## Build

PowerShell:

```powershell
./gradlew.bat assembleDebug
```

bash:

```bash
./gradlew assembleDebug
```

## Run

Use Android Studio Device Manager to start an emulator, then run:

```bash
./gradlew installDebug
```

## Security

- Keep secrets out of source control
- Copy .env.example to .env only when needed for local use
- Do not commit real credentials
