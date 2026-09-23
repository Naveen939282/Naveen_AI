package com.naveenai.app.ai

import com.naveenai.app.actions.ActionDispatcher
import com.naveenai.app.command.AssistantIntent
import com.naveenai.app.command.CommandRouter
import com.naveenai.app.command.LocalIntentClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssistantCoordinator(
    private val commandRouter: CommandRouter = CommandRouter(),
    private val classifier: LocalIntentClassifier = LocalIntentClassifier(),
    private val actionDispatcher: ActionDispatcher = ActionDispatcher(),
    private val aiProvider: AIProvider = OllamaAIProvider(),
) {
    suspend fun process(text: String): AIResponse = withContext(Dispatchers.Default) {
        val classified = classifier.classify(text)
        if (classified.intent == AssistantIntent.TIME ||
            classified.intent == AssistantIntent.DATE ||
            classified.intent == AssistantIntent.OPEN_APP ||
            classified.intent == AssistantIntent.OPEN_URL
        ) {
            val action = actionDispatcher.dispatch(classified.intent, classified.argument)
            return@withContext AIResponse(
                action.success,
                if (action.success) action.message else "",
                action.message.takeUnless { action.success },
            )
        }
        if (classified.intent != AssistantIntent.UNKNOWN) {
            val command = commandRouter.route(text)
            return@withContext AIResponse(command.success, command.response)
        }

        aiProvider.generateResponse(AIRequest(text))
    }
}