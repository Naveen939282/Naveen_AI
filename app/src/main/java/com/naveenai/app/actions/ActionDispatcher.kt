package com.naveenai.app.actions

import com.naveenai.app.command.AssistantIntent

class ActionDispatcher(
    actions: List<AndroidAction> = listOf(TimeAction(), DateAction()),
) {
    private val actionsByIntent = actions.associateBy(AndroidAction::intent)

    suspend fun dispatch(request: ActionRequest): ActionResult {
        return actionsByIntent[request.intent]?.execute(request)
            ?: ActionResult(false, "That Android action is not supported yet.")
    }

    suspend fun dispatch(intent: AssistantIntent, argument: String? = null): ActionResult =
        dispatch(ActionRequest(intent, argument))

    companion object {
        fun forAndroid(context: android.content.Context): ActionDispatcher = ActionDispatcher(
            listOf(TimeAction(), DateAction(), OpenAppAction.forAndroid(context)),
        )
    }
}