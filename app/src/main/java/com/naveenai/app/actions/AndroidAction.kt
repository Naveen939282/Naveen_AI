package com.naveenai.app.actions

import com.naveenai.app.command.AssistantIntent

data class ActionRequest(
    val intent: AssistantIntent,
    val argument: String? = null,
)

interface AndroidAction {
    val intent: AssistantIntent

    suspend fun execute(request: ActionRequest = ActionRequest(intent)): ActionResult
}