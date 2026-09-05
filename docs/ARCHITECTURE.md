# NAVEEN AI Architecture

## Overview

NAVEEN AI is designed as a modular Android personal assistant. The architecture separates concerns so that voice, AI, command execution, device actions, and persistence can evolve independently.

## Layered architecture

### Presentation layer
- Home assistant screen
- Settings screen placeholder
- Future memory and activity history screens

### Voice layer
- `SpeechRecognitionManager` owns Android `SpeechRecognizer` setup, callbacks, errors, and cleanup.
- `TextToSpeechManager` owns native TTS initialization, locale selection, speaking, stopping, and shutdown.
- `VoiceState` represents the user-visible lifecycle: `IDLE`, `REQUESTING_PERMISSION`, `LISTENING`, `PROCESSING`, `AI_THINKING`, `RESPONDING`, `SUCCESS`, and `ERROR`.
- Wake-word detection remains a future concern.

### AI layer
- `AIProvider` accepts an `AIRequest` and returns an application-level `AIResponse`.
- `OllamaAIProvider` isolates local HTTP communication and never requires an API key. Its base URL and model are supplied through `BuildConfig`, populated from `OLLAMA_BASE_URL` and `OLLAMA_MODEL` Gradle properties.
- `AssistantPrompt` centralizes the NAVEEN AI identity and limitation guidance.
- Placeholder providers return explicit unavailable results rather than fake answers.
- Prompt manager
- Response parser

### Command layer
- `IntentClassifier` is the replaceable classification boundary.
- `LocalIntentClassifier` deterministically recognizes `GREETING`, `HELP`, and `UNKNOWN`.
- `CommandRouter` returns a structured `CommandResult`; it does not manipulate Android views.

### Assistant coordination
- `AssistantCoordinator` decides whether input is a deterministic local command or a conversational AI request.
- Local `GREETING` and `HELP` commands do not use the AI provider.
- Other input is processed asynchronously by the configured provider and returned as `AIResponse`.
- The Activity observes results and delegates voice output to `TextToSpeechManager`.

For the Android Emulator, the default provider URL is `http://10.0.2.2:11434`, which maps to the development PC. A physical device uses a PC LAN URL supplied at build time; no LAN address is committed.

### Android integration layer
- App launcher
- Browser launch
- Notifications
- Reminder integration
- Phone and messaging interfaces
- Permission manager

### Memory layer
- User preferences
- Conversation history
- Personal memory
- SQLite database

### Security layer
- API key management
- Secure storage
- Runtime permission checks

### Utilities
- Logging
- Error handling
- Configuration

## Step 1 through Step 3 scope

Step 1 intentionally includes:
- app shell
- UI structure
- permission handling
- config files
- AI interface abstraction
- command architecture skeleton
- Android `SpeechRecognizer` voice input foundation
- microphone permission requested on demand
- text command fallback
- deterministic greeting, help, and unknown command handling
- documentation and tests
- local Ollama AI provider boundary and structured response model
- asynchronous conversation processing
- native text-to-speech with voice controls

It intentionally does not include:
- wake-word or always-listening detection
- cloud AI providers or API credentials
- full actions like reminders, app launching, or automation
