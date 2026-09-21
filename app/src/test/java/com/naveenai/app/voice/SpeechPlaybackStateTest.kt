package com.naveenai.app.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechPlaybackStateTest {
    @Test
    fun tracksCurrentAndLastSentence() {
        val state = SpeechPlaybackState()
        state.beginResponse(listOf("First.", "Second."))
        state.beginSentence("First.")

        assertEquals("First.", state.sentenceToRepeat())
        state.finishSentence()
        state.beginSentence("Second.")
        assertEquals("Second.", state.sentenceToRepeat())
    }

    @Test
    fun newResponseReplacesOldRepeatContext() {
        val state = SpeechPlaybackState()
        state.beginResponse(listOf("Old."))
        state.beginSentence("Old.")
        state.beginResponse(listOf("New."))

        assertEquals(null, state.sentenceToRepeat())
    }

    @Test
    fun stopPreservesCurrentSentenceForRepeat() {
        val state = SpeechPlaybackState()
        state.beginResponse(listOf("Current."))
        state.beginSentence("Current.")
        state.stop()

        assertEquals("Current.", state.sentenceToRepeat())
        assertFalse(state.isSpeaking)
    }

    @Test
    fun remainingSentencesFollowCurrentSentence() {
        val state = SpeechPlaybackState()
        state.beginResponse(listOf("First.", "Second.", "Third."))
        state.beginSentence("Second.")

        assertEquals(listOf("Third."), state.remainingAfter("Second."))
        assertTrue(state.isSpeaking)
    }
}