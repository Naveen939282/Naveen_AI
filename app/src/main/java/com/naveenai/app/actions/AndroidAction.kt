package com.naveenai.app.actions

import com.naveenai.app.command.AssistantIntent

interface AndroidAction {
    val intent: AssistantIntent

    suspend fun execute(): ActionResult
}