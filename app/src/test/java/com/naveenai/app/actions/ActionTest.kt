package com.naveenai.app.actions

import com.naveenai.app.ai.AIRequest
import com.naveenai.app.ai.AIResponse
import com.naveenai.app.ai.AIProvider
import com.naveenai.app.ai.AssistantCoordinator
import com.naveenai.app.command.AssistantIntent
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionResultTest {
    @Test
    fun representsSuccessAndFailure() {
        assertTrue(ActionResult(true, "Done").success)
        assertFalse(ActionResult(false, "Failed").success)
    }
}

class ActionDispatcherTest {
    @Test
    fun selectsActionByIntent() = runBlocking {
        val action = FakeAction(AssistantIntent.TIME, ActionResult(true, "Selected"))

        assertEquals(ActionResult(true, "Selected"), ActionDispatcher(listOf(action)).dispatch(AssistantIntent.TIME))
        assertTrue(action.executed)
    }

    @Test
    fun handlesUnsupportedAction() = runBlocking {
        val result = ActionDispatcher(emptyList()).dispatch(AssistantIntent.TIME)

        assertFalse(result.success)
        assertTrue(result.message.isNotEmpty())
    }
}

class TimeAndDateActionTest {
    private val clock = Clock.fixed(Instant.parse("2026-09-21T16:30:00Z"), ZoneId.of("UTC"))

    @Test
    fun timeActionReturnsSuccessAndReadableOutput() = runBlocking {
        val result = TimeAction(clock).execute()

        assertTrue(result.success)
        assertTrue(result.message.isNotEmpty())
    }

    @Test
    fun dateActionReturnsSuccessAndReadableOutput() = runBlocking {
        val result = DateAction(clock).execute()

        assertTrue(result.success)
        assertTrue(result.message.isNotEmpty())
    }
}

class ActionCoordinatorTest {
    @Test
    fun timeAndDateDoNotCallOllama() = runBlocking {
        val provider = RecordingAIProvider()
        val coordinator = AssistantCoordinator(aiProvider = provider)

        assertTrue(coordinator.process("What time is it?").success)
        assertTrue(coordinator.process("What's today's date?").success)
        assertEquals(0, provider.calls)
    }
}

private class FakeAction(
    override val intent: AssistantIntent,
    private val result: ActionResult,
) : AndroidAction {
    var executed = false

    override suspend fun execute(): ActionResult {
        executed = true
        return result
    }
}

private class RecordingAIProvider : AIProvider {
    var calls = 0

    override suspend fun generateResponse(request: AIRequest): AIResponse {
        calls++
        return AIResponse(true, "Unexpected provider call")
    }

    override fun isAvailable(): Boolean = true
}