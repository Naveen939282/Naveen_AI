package com.naveenai.app.command

enum class AssistantIntent {
    GREETING,
    HELP,
    UNKNOWN,
}

data class ClassifiedIntent(
    val rawText: String,
    val intent: AssistantIntent,
    val confidence: Float,
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
            else -> AssistantIntent.UNKNOWN
        }

        return ClassifiedIntent(
            rawText = rawText.trim(),
            intent = intent,
            confidence = if (intent == AssistantIntent.UNKNOWN) 0.2f else 1.0f,
        )
    }
}

class CommandRouter(
    private val classifier: IntentClassifier = LocalIntentClassifier(),
) {
    fun route(rawText: String): CommandResult {
        val classified = classifier.classify(rawText)
        val response = when (classified.intent) {
            AssistantIntent.GREETING -> "Hello! How can I help you?"
            AssistantIntent.HELP -> "I am being developed to help with commands, information, and Android actions. Currently supported: greetings and help."
            AssistantIntent.UNKNOWN -> "I didn't understand that command yet."
        }

        return CommandResult(
            success = classified.intent != AssistantIntent.UNKNOWN,
            intent = classified.intent,
            response = response,
        )
    }
}
