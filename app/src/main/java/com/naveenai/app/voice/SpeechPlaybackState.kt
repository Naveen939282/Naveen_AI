package com.naveenai.app.voice

class SpeechPlaybackState {
    var currentSentence: String? = null
        private set

    var lastSpokenSentence: String? = null
        private set

    var isSpeaking: Boolean = false
        private set

    private var responseSentences: List<String> = emptyList()

    fun beginResponse(sentences: List<String> = emptyList()) {
        currentSentence = null
        lastSpokenSentence = null
        isSpeaking = false
        responseSentences = sentences
    }

    fun beginSentence(sentence: String) {
        currentSentence = sentence
        isSpeaking = true
    }

    fun updateSentence(sentence: String) {
        currentSentence = sentence
        isSpeaking = true
    }

    fun finishSentence() {
        currentSentence?.let { lastSpokenSentence = it }
        currentSentence = null
    }

    fun finishPlayback() {
        finishSentence()
        isSpeaking = false
    }

    fun stop() {
        currentSentence?.let { lastSpokenSentence = it }
        currentSentence = null
        isSpeaking = false
    }

    fun sentenceToRepeat(): String? = currentSentence ?: lastSpokenSentence

    fun remainingAfter(sentence: String): List<String> {
        val index = responseSentences.indexOf(sentence)
        return if (index >= 0) responseSentences.drop(index + 1) else emptyList()
    }
}