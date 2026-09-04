package com.naveenai.app.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AIProviderTest {
    @Test
    fun localProviderIsNotAvailableInStepOne() {
        val provider = LocalAIProvider()
        assertFalse(provider.isAvailable())
        assertTrue(provider.generateResponse("Test").isNotEmpty())
    }

    @Test
    fun cloudProviderIsNotAvailableInStepOne() {
        val provider = FreeCloudAIProvider()
        assertFalse(provider.isAvailable())
        assertTrue(provider.generateResponse("Test").isNotEmpty())
    }
}
