package com.naveenai.app.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechTextChunkerTest {
    @Test
    fun groupsShortSentencesToReduceUtteranceBoundaries() {
        val chunks = SpeechTextChunker.chunk("First sentence. Second sentence! Third sentence?")

        assertEquals(listOf("First sentence. Second sentence! Third sentence?"), chunks)
    }

    @Test
    fun keepsColonsAndSemicolonsInsideTheSentence() {
        val chunks = SpeechTextChunker.chunk("Answer: first point; second point\nThird point.")

        assertEquals(listOf("Answer: first point; second point Third point."), chunks)
    }

    @Test
    fun removesCommonMarkdownWithoutDroppingWords() {
        val chunks = SpeechTextChunker.chunk("## **Answer**\n- [Read more](https://example.com).")

        assertEquals(listOf("Answer Read more."), chunks)
    }

    @Test
    fun splitsAnOverlongSentenceOnlyAtWhitespace() {
        val chunks = SpeechTextChunker.chunk("one two three four five six", maxLength = 10)

        assertTrue(chunks.all { it.length <= 10 })
        assertEquals("one two", chunks[0])
        assertEquals("three four", chunks[1])
        assertEquals("five six", chunks[2])
    }
}