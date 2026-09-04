package com.naveenai.app.command

data class CommandIntent(
    val rawText: String,
    val actionType: String,
    val confidence: Float,
)

interface CommandClassifier {
    fun classify(rawText: String): CommandIntent
}

class SimpleCommandClassifier : CommandClassifier {
    override fun classify(rawText: String): CommandIntent {
        val normalized = rawText.trim()
        val action = when {
            normalized.isEmpty() -> "unknown"
            normalized.contains("open", ignoreCase = true) -> "open_app"
            normalized.contains("search", ignoreCase = true) -> "web_search"
            normalized.contains("reminder", ignoreCase = true) -> "set_reminder"
            normalized.contains("weather", ignoreCase = true) -> "weather"
            normalized.contains("explain", ignoreCase = true) -> "explain"
            else -> "general_chat"
        }

        return CommandIntent(
            rawText = normalized,
            actionType = action,
            confidence = 0.5f,
        )
    }
}

interface ActionExecutor {
    fun execute(intent: CommandIntent): String
}

class AssistantActionExecutor : ActionExecutor {
    override fun execute(intent: CommandIntent): String {
        return "Action execution for '${intent.actionType}' is planned for a future step."
    }
}

class CommandRouter(
    private val classifier: CommandClassifier = SimpleCommandClassifier(),
    private val executor: ActionExecutor = AssistantActionExecutor(),
) {
    fun route(rawText: String): String {
        val intent = classifier.classify(rawText)
        return executor.execute(intent)
    }
}
