package com.naveenai.app.actions

import com.naveenai.app.command.AssistantIntent

class ActionDispatcher(
    actions: List<AndroidAction> = listOf(TimeAction(), DateAction()),
) {
    private val actionsByIntent = actions.associateBy(AndroidAction::intent)

    suspend fun dispatch(intent: AssistantIntent): ActionResult {
        return actionsByIntent[intent]?.execute()
            ?: ActionResult(false, "That Android action is not supported yet.")
    }
}