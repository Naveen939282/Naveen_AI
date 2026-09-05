package com.naveenai.app.voice

enum class VoiceState {
    IDLE,
    REQUESTING_PERMISSION,
    LISTENING,
    PROCESSING,
    AI_THINKING,
    RESPONDING,
    SUCCESS,
    ERROR,
}