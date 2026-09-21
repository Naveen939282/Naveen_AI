package com.naveenai.app.voice

object SpeechTextChunker {
    private const val MAX_CHUNK_LENGTH = 900

    data class SpeechChunk(
        val text: String,
        val sentences: List<String>,
    )

    fun chunk(text: String, maxLength: Int = MAX_CHUNK_LENGTH): List<String> {
        require(maxLength > 0) { "maxLength must be positive" }

        val cleaned = cleanMarkdown(text)
        if (cleaned.isBlank()) return emptyList()

        return chunksWithSentences(text, maxLength).map { it.text }
    }

    fun chunksWithSentences(text: String, maxLength: Int = MAX_CHUNK_LENGTH): List<SpeechChunk> {
        require(maxLength > 0) { "maxLength must be positive" }

        val cleaned = cleanMarkdown(text)
        if (cleaned.isBlank()) return emptyList()

        val chunks = mutableListOf<SpeechChunk>()
        var currentSentences = mutableListOf<String>()
        var currentText = ""

        sentenceParts(cleaned)
            .flatMap { sentence -> splitLongSentence(sentence, maxLength) }
            .forEach { sentence ->
                val candidate = if (currentText.isEmpty()) sentence else "$currentText $sentence"
                if (candidate.length <= maxLength) {
                    currentText = candidate
                    currentSentences += sentence
                } else {
                    chunks += SpeechChunk(currentText, currentSentences)
                    currentText = sentence
                    currentSentences = mutableListOf(sentence)
                }
            }

        if (currentText.isNotEmpty()) chunks += SpeechChunk(currentText, currentSentences)
        return chunks
    }

    private fun cleanMarkdown(text: String): String = text
        .replace(Regex("```(?:[A-Za-z0-9_+-]+)?\\s*"), "")
        .replace("```", "")
        .replace(Regex("!\\[([^]]*)]\\([^)]*\\)"), "$1")
        .replace(Regex("\\[([^]]+)]\\([^)]*\\)"), "$1")
        .replace(Regex("^\\s{0,3}#{1,6}\\s*", RegexOption.MULTILINE), "")
        .replace(Regex("^\\s*[-*+]\\s+", RegexOption.MULTILINE), "")
        .replace(Regex("^\\s*\\d+[.)]\\s+", RegexOption.MULTILINE), "")
        .replace(Regex("[*_~`]"), "")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun sentenceParts(text: String): List<String> {
        val parts = mutableListOf<String>()
        val current = StringBuilder()

        text.forEach { character ->
            current.append(character)
            if (character == '.' || character == '!' || character == '?') {
                parts += current.toString().trim()
                current.clear()
            }
        }

        if (current.isNotBlank()) parts += current.toString().trim()
        return parts
    }

    private fun splitLongSentence(sentence: String, maxLength: Int): List<String> {
        if (sentence.length <= maxLength) return listOf(sentence)

        val chunks = mutableListOf<String>()
        var remaining = sentence.trim()
        while (remaining.length > maxLength) {
            var splitAt = remaining.lastIndexOf(' ', maxLength)
            if (splitAt <= 0) splitAt = maxLength
            chunks += remaining.substring(0, splitAt).trim()
            remaining = remaining.substring(splitAt).trim()
        }
        if (remaining.isNotEmpty()) chunks += remaining
        return chunks
    }
}