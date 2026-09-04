# NAVEEN AI

NAVEEN AI is a production-oriented Android personal assistant project inspired by a JARVIS-like experience, designed to be built step by step with free and open-source tooling.

## 1. Project overview

This project is being built as a modular Android application focused on:

- wake word activation using "Naveen"
- speech-to-text and text-to-speech
- command classification and intent routing
- local and cloud AI integration
- Android automation where permissions allow it
- secure local storage and configuration

The current step focuses on a clean foundation, architecture, and a working initial app shell with a microphone UI and status indicator.

## 2. Features planned

### Core features
- Wake word detection
- Speech recognition
- Command classification
- Natural-language intent handling
- Local AI provider interface
- Free cloud AI provider abstraction
- Reminders and notifications
- App launching and device actions
- Local memory and conversation history
- SQLite-backed persistence

### Later features
- PDF/document summarization
- Camera and vision features
- Advanced personalization and memory graphs
- Offline local model execution
- Expanded Android integrations

## 3. Technology stack

### Primary technology: Native Android + Kotlin
Purpose: Android app development, permissions, microphone access, wake-word service, notifications, AI integration, local storage.

Why we need it:
- Best Android runtime performance and permission control
- Native access to speech, notifications, app launching, and Android APIs
- Strong future support for background services and local AI models
- Clear path to future modular architecture

Free/open-source status:
- Android SDK: free and open-source
- Kotlin: free and open-source
- Android Studio: free and open-source tooling

Alternative:
- Flutter + Dart: easier UI prototyping but weaker native Android feature integration and more complexity for wake-word and background services
- React Native: cross-platform but with more bridging overhead for deep Android features and permissions

## 4. Architecture

The project follows a modular layered architecture:

- Presentation layer
- Voice layer
- AI layer
- Command layer
- Android integration layer
- Memory layer
- Security layer
- Utilities

See docs/ARCHITECTURE.md for the full structure.

## 5. Installation requirements

- Windows, macOS, or Linux
- Android Studio with SDK Manager
- JDK 17 or newer
- Git
- VS Code (recommended)

## 6. VS Code setup

1. Install VS Code.
2. Install the Android Studio extension pack if desired.
3. Open the project folder in VS Code.
4. Ensure the Android SDK path is configured.
5. Use the Gradle wrapper or Android Studio to build.

## 7. Android setup

1. Install Android Studio.
2. Install Android SDK Platform 36 and build-tools 36.0.0.
3. Set up an Android emulator or physical device.
4. Enable developer options and USB debugging if needed.

## 8. How to run

From the project root:

```bash
./gradlew assembleDebug
```

Or on Windows:

```powershell
gradlew.bat assembleDebug
```

Then install the APK to an emulator or connected Android device.

## 9. How to build APK

```bash
./gradlew assembleRelease
```

For a debug build:

```bash
./gradlew assembleDebug
```

## 10. Free-cost strategy

- Use Android SDK and Kotlin free of charge
- Use local SQLite storage
- Use open-source models when practical
- Prefer local AI and on-device inference where possible
- Avoid paid cloud services by default
- Use free tiers only when clearly documented and optional

## 11. Security notes

- Never commit secrets to source control
- Use .env.example as a template only
- Keep API keys out of code and out of Git history
- Request only the permissions that are needed for current features
- Use secure local storage patterns for future credentials

## 12. Future roadmap

### Step 1
- Project foundation
- Architecture design
- Initial app shell
- Documentation and setup

### Step 2
- Wake word and microphone workflow
- Intent classification and command routing
- Local AI interface skeleton

### Step 3
- Speech recognition
- TTS flow
- SQLite memory layer

### Step 4
- Android integrations
- Reminders and notifications
- App opening and automation

### Step 5
- PDF summarization and document flow
- Offline AI workflows

## 13. Known limitations

- Real voice activation and AI behavior are intentionally not implemented in Step 1.
- Advanced Android integrations require extra permissions and OS restrictions.
- Some features require emulator or device-specific configuration.
- Free AI providers have service and rate limits that should be reviewed before use.

## 14. Repository status

This repository is initialized for Step 1 and is intentionally minimal, modular, and buildable.
