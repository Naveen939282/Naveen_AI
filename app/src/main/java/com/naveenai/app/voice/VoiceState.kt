package com.naveenai.app.voice

enum class VoiceState {
    IDLE,
    REQUESTING_PERMISSION,
    LISTENING,
    PROCESSING,
    SUCCESS,
    ERROR,
}