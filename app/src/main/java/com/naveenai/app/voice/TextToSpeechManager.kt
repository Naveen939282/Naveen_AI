package com.naveenai.app.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechManager(
    context: Context,
    private val listener: Listener,
) : TextToSpeech.OnInitListener {
    interface Listener {
        fun onReady()
        fun onUnavailable(message: String)
    }

    private val textToSpeech = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            listener.onUnavailable("Voice output is unavailable.")
            return
        }

        val result = textToSpeech.setLanguage(Locale.getDefault())
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            listener.onUnavailable("Voice output is not available for this language.")
        } else {
            ready = true
            listener.onReady()
        }
    }

    fun speak(text: String) {
        if (ready && text.isNotBlank()) {
            textToSpeech.speak(text.take(MAX_SPOKEN_CHARACTERS), TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        }
    }

    fun stop() {
        textToSpeech.stop()
    }

    fun release() {
        ready = false
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    private companion object {
        const val MAX_SPOKEN_CHARACTERS = 1_000
        const val UTTERANCE_ID = "naveen-ai-response"
    }
}