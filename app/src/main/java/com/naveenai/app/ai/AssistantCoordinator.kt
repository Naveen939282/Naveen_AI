package com.naveenai.app.ai

import com.naveenai.app.command.AssistantIntent
import com.naveenai.app.command.CommandRouter
import com.naveenai.app.command.LocalIntentClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssistantCoordinator(
    private val commandRouter: CommandRouter = CommandRouter(),
    private val classifier: LocalIntentClassifier = LocalIntentClassifier(),
    private val aiProvider: AIProvider = OllamaAIProvider(),
) {
    suspend fun process(text: String): AIResponse = withContext(Dispatchers.Default) {
        val classified = classifier.classify(text)
        if (classified.intent != AssistantIntent.UNKNOWN) {
            val command = commandRouter.route(text)
            return@withContext AIResponse(command.success, command.response)
        }

        aiProvider.generateResponse(AIRequest(text))
    }
}