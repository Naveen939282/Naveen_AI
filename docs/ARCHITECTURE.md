# NAVEEN AI Architecture

## Overview

NAVEEN AI is designed as a modular Android personal assistant. The architecture separates concerns so that voice, AI, command execution, device actions, and persistence can evolve independently.

## Layered architecture

### Presentation layer
- Home assistant screen
- Settings screen placeholder
- Future memory and activity history screens

### Voice layer
- Wake word detection
- Speech recognition
- Text to speech

### AI layer
- AI provider abstraction
- Local AI provider
- Free cloud AI provider
- Prompt manager
- Response parser

### Command layer
- Command classifier
- Command router
- Intent handler
- Action executor

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

## Step 1 scope

Step 1 intentionally includes:
- app shell
- UI structure
- permission handling
- config files
- AI interface abstraction
- command architecture skeleton
- documentation

It intentionally does not include:
- production wake word detection
- live speech recognition implementation
- real AI provider integration
- full actions like reminders and app launching
