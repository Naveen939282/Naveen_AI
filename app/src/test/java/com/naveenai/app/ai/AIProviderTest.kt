package com.naveenai.app.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals

class AIProviderTest {
    @Test
    fun localProviderReportsUnavailableWithoutInventingResponse() = runBlocking {
        val response = LocalAIProvider().generateResponse(AIRequest("Test"))

        assertFalse(response.success)
        assertTrue(response.text.isEmpty())
        assertEquals("No local AI model is configured.", response.errorMessage)
    }

    @Test
    fun cloudProviderReportsUnavailableWithoutInventingResponse() = runBlocking {
        val response = FreeCloudAIProvider().generateResponse(AIRequest("Test"))

        assertFalse(response.success)
        assertTrue(response.text.isEmpty())
        assertEquals("No cloud AI provider is configured.", response.errorMessage)
    }
}

class AssistantCoordinatorTest {
    @Test
    fun routesConversationToFakeProvider() = runBlocking {
        val provider = FakeAIProvider(AIResponse(true, "Test AI response"))
        val coordinator = AssistantCoordinator(aiProvider = provider)

        val response = coordinator.process("Explain machine learning")

        assertTrue(response.success)
        assertEquals("Test AI response", response.text)
        assertEquals("Explain machine learning", provider.lastRequest?.userMessage)
    }

    @Test
    fun keepsLocalGreetingIndependentOfProvider() = runBlocking {
        val provider = FakeAIProvider(AIResponse(false, "", "Provider should not be called"))
        val response = AssistantCoordinator(aiProvider = provider).process("hello")

        assertTrue(response.success)
        assertEquals("Hello! How can I help you?", response.text)
        assertEquals(null, provider.lastRequest)
    }

    @Test
    fun returnsProviderFailureWithoutInventingAnswer() = runBlocking {
        val response = AssistantCoordinator(
            aiProvider = FakeAIProvider(AIResponse(false, "", "AI service is unavailable.")),
        ).process("Explain neural networks")

        assertFalse(response.success)
        assertEquals("AI service is unavailable.", response.errorMessage)
    }
}

private class FakeAIProvider(
    private val response: AIResponse,
) : AIProvider {
    var lastRequest: AIRequest? = null

    override suspend fun generateResponse(request: AIRequest): AIResponse {
        lastRequest = request
        return response
    }

    override fun isAvailable(): Boolean = true
}
