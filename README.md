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

The current step provides a conversational foundation with Android speech recognition, deterministic local command routing, an optional local Ollama AI provider, native text-to-speech, and a text fallback.

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

## 9. Step 2 voice interaction

The Step 2 flow is:

```text
Microphone -> SpeechRecognizer -> recognized text -> LocalIntentClassifier
-> CommandRouter -> CommandResult -> response UI
```

Tap the microphone to request `RECORD_AUDIO` only when needed and start Android's built-in `SpeechRecognizer`. The screen exposes `IDLE`, `REQUESTING_PERMISSION`, `LISTENING`, `PROCESSING`, `SUCCESS`, and `ERROR` states. A text field and Send button provide a development fallback when a microphone or recognizer is unavailable.

Currently supported local commands are greetings (`hello`, `hi`, `hey`, `good morning`, and `good evening`) and help (`help`, `commands`, `available commands`, and `what can you do`). Other input returns an explicit unsupported-command response.

## 10. Step 3 AI brain and voice output

Step 3 routes local commands directly to `CommandRouter`. Other text is sent through `AssistantCoordinator` to the replaceable `AIProvider` abstraction. The configured provider is `OllamaAIProvider`, which uses a local Ollama server at the Android emulator host address (`http://10.0.2.2:11434`) and the `llama3` model by default. No API key or cloud account is required.

Successful responses are displayed and optionally spoken with Android's native `TextToSpeech`. Voice output can be toggled or stopped from the main screen. Provider failures remain visible as honest errors, while local commands continue to work without Ollama.

The current Step 3 flow is:

```text
Input -> AssistantCoordinator -> local command OR AIProvider
	-> AIResponse -> response UI -> optional TextToSpeech
```

To use local AI, install Ollama on the development machine, pull the configured model, and make the server reachable by the emulator. A physical device needs a reachable address configured through the `OllamaAIProvider` constructor; no credentials belong in the repository.

## 11. How to build APK

```bash
./gradlew assembleRelease
```

For a debug build:

```bash
./gradlew assembleDebug
```

## 12. Free-cost strategy

- Use Android SDK and Kotlin free of charge
- Use local SQLite storage
- Use open-source models when practical
- Prefer local AI and on-device inference where possible
- Avoid paid cloud services by default
- Use free tiers only when clearly documented and optional

## 13. Security notes

- Never commit secrets to source control
- Use .env.example as a template only
- Keep API keys out of code and out of Git history
- Request only the permissions that are needed for current features
- Use secure local storage patterns for future credentials

## 14. Future roadmap

### Step 1
- Project foundation
- Architecture design
- Initial app shell
- Documentation and setup

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

## 15. Known limitations

- Wake-word detection, background listening, and Android automation are not implemented.
- Long-term memory, vision, and cloud AI providers are not implemented.
- Android `SpeechRecognizer` availability and network behavior vary by device and installed speech service.
- Ollama must be installed, have the selected model available, and be reachable from the device for conversational responses.
- Advanced Android integrations require extra permissions and OS restrictions.
- Some features require emulator or device-specific configuration.
- Free AI providers have service and rate limits that should be reviewed before use.

## 16. Repository status

This repository contains the Step 1 foundation, Step 2 voice interaction foundation, and Step 3 AI brain/TTS foundation. It remains intentionally minimal, modular, and buildable.
