package com.naveenai.app.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
    private var initialized = false
    private var pendingText: String? = null
    private var playbackGeneration = 0L
    private val playbackState = SpeechPlaybackState()
    private val utteranceSentences = mutableMapOf<String, List<String>>()

    override fun onInit(status: Int) {
        initialized = true
        if (status != TextToSpeech.SUCCESS) {
            listener.onUnavailable("Voice output is unavailable.")
            return
        }

        val result = textToSpeech.setLanguage(Locale.getDefault())
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            listener.onUnavailable("Voice output is not available for this language.")
            return
        }

        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                utteranceSentences[utteranceId]?.firstOrNull()?.let { playbackState.beginSentence(it) }
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                val sentences = utteranceSentences[utteranceId] ?: return
                var offset = 0
                sentences.forEach { sentence ->
                    val sentenceStart = offset
                    val sentenceEnd = sentenceStart + sentence.length
                    if (start in sentenceStart until sentenceEnd) playbackState.updateSentence(sentence)
                    offset = sentenceEnd + 1
                }
            }

            override fun onDone(utteranceId: String?) {
                utteranceSentences[utteranceId]?.lastOrNull()?.let { playbackState.beginSentence(it) }
                playbackState.finishSentence()
                utteranceSentences.remove(utteranceId)
            }

            override fun onError(utteranceId: String?) = Unit
        })
        ready = true
        listener.onReady()
        pendingText?.let {
            pendingText = null
            speakNow(it)
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        utteranceSentences.clear()
        val sentences = SpeechTextChunker.chunksWithSentences(text).flatMap { it.sentences }
        playbackState.beginResponse(sentences)
        if (!initialized) {
            pendingText = text
        } else if (ready) {
            speakNow(text)
        }
    }

    fun stop() {
        playbackGeneration++
        pendingText = null
        textToSpeech.stop()
        utteranceSentences.clear()
        playbackState.stop()
    }

    fun repeatCurrentSentence(shouldSpeak: Boolean = true): String? {
        val sentence = playbackState.sentenceToRepeat() ?: return null
        val continuation = playbackState.remainingAfter(sentence)
        if (shouldSpeak) {
            textToSpeech.stop()
            utteranceSentences.clear()
            speakNow((listOf(sentence) + continuation).joinToString(" "))
        }
        return sentence
    }

    fun release() {
        playbackGeneration++
        pendingText = null
        ready = false
        initialized = false
        textToSpeech.stop()
        textToSpeech.shutdown()
        utteranceSentences.clear()
        playbackState.stop()
    }

    private fun speakNow(text: String) {
        val generation = playbackGeneration++
        SpeechTextChunker.chunksWithSentences(text).forEachIndexed { index, chunk ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val utteranceId = "$UTTERANCE_ID-$generation-$index"
            utteranceSentences[utteranceId] = chunk.sentences
            textToSpeech.speak(
                chunk.text,
                queueMode,
                null,
                utteranceId,
            )
        }
    }

    private companion object {
        const val UTTERANCE_ID = "naveen-ai-response"
    }
}
