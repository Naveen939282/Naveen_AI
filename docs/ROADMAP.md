# NAVEEN AI Roadmap

## Step 1
- Choose the technology stack
- Define the architecture
- Initialize the Android project
- Create a clean UI skeleton
- Add configuration and security templates
- Add AI and command abstraction layers
- Write initial tests and docs

## Step 2
- Implement microphone workflow
- Integrate Android `SpeechRecognizer`
- Add on-demand microphone permission handling
- Add voice state model and lifecycle cleanup
- Add deterministic local `GREETING`, `HELP`, and `UNKNOWN` classification
- Connect structured command results to the app shell
- Add text-input fallback and unit tests

Step 2 did not include wake-word detection, text-to-speech, external AI, background services, or Android automation.

## Step 3
- Add replaceable AI request/response abstractions
- Route non-local conversation through local Ollama
- Keep local commands available when AI is unavailable
- Add asynchronous assistant coordination
- Add native text-to-speech with toggle and stop controls
- Add fake-provider and failure-path unit tests

Step 3 does not include wake-word detection, background listening, long-term memory, cloud credentials, or Android automation.

## Step 4
- Add Android integrations
- Implement reminder and notification support
- Add app-launch and browser integration

## Step 5
- Add external AI provider bridge
- Support local free AI models
- Add PDF summary flow

## Future
- Camera and vision support
- Context memory and personalization
- Offline-first capability improvements
- Advanced UX and assistant actions
