package com.naveenai.app.command

enum class AssistantIntent {
    GREETING,
    HELP,
    REPEAT,
    TIME,
    DATE,
    OPEN_APP,
    OPEN_URL,
    UNKNOWN,
}

data class ClassifiedIntent(
    val rawText: String,
    val intent: AssistantIntent,
    val confidence: Float,
    val argument: String? = null,
)

data class CommandResult(
    val success: Boolean,
    val intent: AssistantIntent,
    val response: String,
)

interface IntentClassifier {
    fun classify(rawText: String): ClassifiedIntent
}

class LocalIntentClassifier : IntentClassifier {
    override fun classify(rawText: String): ClassifiedIntent {
        val normalized = rawText.trim().lowercase()
        val intent = when {
            normalized.isEmpty() -> AssistantIntent.UNKNOWN
            normalized.matches(Regex("(hi|hello|hey)( naveen)?[.!?]*")) -> AssistantIntent.GREETING
            normalized.matches(Regex("(good morning|good evening)[.!?]*")) -> AssistantIntent.GREETING
            normalized == "help" || normalized == "commands" ||
                normalized == "available commands" || normalized == "what can you do" -> AssistantIntent.HELP
            normalized.matches(Regex("(repeat that|repeat that sentence|repeat the last sentence|repeat the line|say that again( please)?|again|what did you say)[.!?]*")) -> AssistantIntent.REPEAT
            normalized.matches(Regex("(what time is it|what's the time|tell me the time|current time|time please)[.!?]*")) -> AssistantIntent.TIME
            normalized.matches(Regex("(what's today's date|what is the date|tell me today's date|today's date|what day is it)[.!?]*")) -> AssistantIntent.DATE
            openUrlMatch(normalized) != null -> AssistantIntent.OPEN_URL
            openAppMatch(normalized) != null -> AssistantIntent.OPEN_APP
            else -> AssistantIntent.UNKNOWN
        }

        return ClassifiedIntent(
            rawText = rawText.trim(),
            intent = intent,
            confidence = if (intent == AssistantIntent.UNKNOWN) 0.2f else 1.0f,
            argument = when (intent) {
                AssistantIntent.OPEN_URL -> openUrlMatch(normalized)?.groupValues?.get(2)?.trim()
                AssistantIntent.OPEN_APP -> openAppMatch(normalized)?.groupValues?.get(3)?.trim()
                else -> null
            },
        )
    }

    private fun openUrlMatch(normalized: String): MatchResult? =
        Regex("^(open|go to|visit)\\s+(.+?)[.!?]*$")
            .matchEntire(normalized)
            ?.takeIf { isWebTarget(it.groupValues[2]) }

    private fun isWebTarget(target: String): Boolean =
        target in setOf("google", "github") ||
            target.startsWith("http://") ||
            target.startsWith("https://") ||
            target.startsWith("www.") ||
            target.matches(Regex("[a-z0-9-]+(\\.[a-z0-9-]+)+"))

    private fun openAppMatch(normalized: String): MatchResult? =
        Regex("^(open|launch|start)\\s+(the\\s+)?(.+?)[.!?]*$").matchEntire(normalized)
}

class CommandRouter(
    private val classifier: IntentClassifier = LocalIntentClassifier(),
) {
    fun route(rawText: String): CommandResult {
        val classified = classifier.classify(rawText)
        val response = when (classified.intent) {
            AssistantIntent.GREETING -> "Hello! How can I help you?"
            AssistantIntent.HELP -> "I am being developed to help with commands, information, and Android actions. Currently supported: greetings and help."
            AssistantIntent.REPEAT -> "There isn't anything to repeat yet."
            AssistantIntent.TIME -> ""
            AssistantIntent.DATE -> ""
            AssistantIntent.OPEN_APP -> ""
            AssistantIntent.OPEN_URL -> ""
            AssistantIntent.UNKNOWN -> "I didn't understand that command yet."
        }

        return CommandResult(
            success = classified.intent != AssistantIntent.UNKNOWN,
            intent = classified.intent,
            response = response,
        )
    }
}
