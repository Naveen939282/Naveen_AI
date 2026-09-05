package com.naveenai.app.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import java.util.Locale

class SpeechRecognitionManager(
    context: Context,
    private val listener: Listener,
) : RecognitionListener {

    interface Listener {
        fun onStateChanged(state: VoiceState)
        fun onTextRecognized(text: String)
        fun onError(message: String)
    }

    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private var listening = false

    init {
        if (SpeechRecognizer.isRecognitionAvailable(appContext)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).also {
                it.setRecognitionListener(this)
            }
        }
    }

    fun isAvailable(): Boolean = recognizer != null

    fun startListening(): Boolean {
        if (listening) {
            return true
        }

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notifyError("Microphone permission is required.")
            return false
        }

        val speechRecognizer = recognizer
        if (speechRecognizer == null) {
            notifyError("Speech recognition is not available on this device.")
            return false
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        listening = true
        listener.onStateChanged(VoiceState.LISTENING)
        speechRecognizer.startListening(intent)
        return true
    }

    fun stopListening() {
        listening = false
        recognizer?.stopListening()
    }

    fun release() {
        listening = false
        recognizer?.setRecognitionListener(null)
        recognizer?.destroy()
        recognizer = null
    }

    override fun onReadyForSpeech(params: Bundle?) {
        listener.onStateChanged(VoiceState.LISTENING)
    }

    override fun onBeginningOfSpeech() {
        listener.onStateChanged(VoiceState.LISTENING)
    }

    override fun onEndOfSpeech() {
        listener.onStateChanged(VoiceState.PROCESSING)
    }

    override fun onResults(results: Bundle?) {
        listening = false
        val text = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.trim()

        if (text.isNullOrEmpty()) {
            notifyError("I did not hear a command. Please try again.")
        } else {
            listener.onTextRecognized(text)
        }
    }

    override fun onError(error: Int) {
        listening = false
        notifyError(errorMessage(error))
    }

    override fun onRmsChanged(rmsdB: Float) = Unit

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onPartialResults(partialResults: Bundle?) = Unit

    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    private fun notifyError(message: String) {
        listener.onStateChanged(VoiceState.ERROR)
        listener.onError(message)
    }

    private fun errorMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "The microphone could not be used."
        SpeechRecognizer.ERROR_CLIENT -> "Speech recognition was cancelled."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition needs a network connection."
        SpeechRecognizer.ERROR_NO_MATCH,
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I did not hear a command. Please try again."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition is busy. Please try again."
        else -> "Speech recognition failed. Please try again."
    }
}