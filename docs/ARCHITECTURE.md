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
- `VoiceState` represents the user-visible voice lifecycle: `IDLE`, `REQUESTING_PERMISSION`, `LISTENING`, `PROCESSING`, `SUCCESS`, and `ERROR`.
- Wake-word detection and text-to-speech remain future concerns.

### AI layer
- AI provider abstraction
- Local AI provider
- Free cloud AI provider
- Prompt manager
- Response parser

### Command layer
- `IntentClassifier` is the replaceable classification boundary.
- `LocalIntentClassifier` deterministically recognizes `GREETING`, `HELP`, and `UNKNOWN`.
- `CommandRouter` returns a structured `CommandResult`; it does not manipulate Android views.

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

## Step 1 and Step 2 scope

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

It intentionally does not include:
- wake-word or always-listening detection
- text-to-speech
- real AI provider integration
- full actions like reminders, app launching, or automation
