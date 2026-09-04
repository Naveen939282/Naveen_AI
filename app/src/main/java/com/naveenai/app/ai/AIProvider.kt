package com.naveenai.app.ai

interface AIProvider {
    fun generateResponse(prompt: String): String
    fun isAvailable(): Boolean
}

class LocalAIProvider : AIProvider {
    override fun generateResponse(prompt: String): String {
        return "Local AI provider is not configured in Step 1. This interface is ready for future implementation."
    }

    override fun isAvailable(): Boolean = false
}

class FreeCloudAIProvider : AIProvider {
    override fun generateResponse(prompt: String): String {
        return "Free cloud AI provider is not configured in Step 1. This interface is ready for future implementation."
    }

    override fun isAvailable(): Boolean = false
}
