package com.naveenai.app.ai

import com.naveenai.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class AIRequest(
    val userMessage: String,
    val systemPrompt: String = AssistantPrompt.system,
)

data class AIResponse(
    val success: Boolean,
    val text: String,
    val errorMessage: String? = null,
)

interface AIProvider {
    suspend fun generateResponse(request: AIRequest): AIResponse
    fun isAvailable(): Boolean
}

object AssistantPrompt {
    const val system = "You are NAVEEN AI, a personal AI assistant. Be helpful, clear, friendly, concise, and honest about limitations. Do not claim to perform actions that are not implemented."
}

class OllamaAIProvider(
    private val baseUrl: String = BuildConfig.OLLAMA_BASE_URL,
    private val model: String = BuildConfig.OLLAMA_MODEL,
) : AIProvider {
    override suspend fun generateResponse(request: AIRequest): AIResponse = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL("${baseUrl.trimEnd('/')}/api/chat").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 60_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            val messages = JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", request.systemPrompt))
                put(JSONObject().put("role", "user").put("content", request.userMessage))
            }
            val body = JSONObject().apply {
                put("model", model)
                put("stream", false)
                put("messages", messages)
            }.toString()

            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val responseCode = connection.responseCode
            val responseBody = (if (responseCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            if (responseCode !in 200..299) {
                val modelError = runCatching { JSONObject(responseBody).optString("error") }.getOrNull()
                val message = if (modelError?.contains("model", ignoreCase = true) == true &&
                    modelError.contains("not found", ignoreCase = true)
                ) {
                    "Ollama model '$model' is not available. Run 'ollama list' and install the selected model."
                } else {
                    "The local AI service returned an error."
                }
                return@withContext AIResponse(false, "", message)
            }

            val text = JSONObject(responseBody).optJSONObject("message")?.optString("content").orEmpty().trim()
            if (text.isEmpty()) {
                AIResponse(false, "", "The local AI service returned an empty response.")
            } else {
                AIResponse(true, text)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: IOException) {
            AIResponse(false, "", "I couldn't reach the local AI service. Start Ollama and try again.")
        } catch (_: Exception) {
            AIResponse(false, "", "The local AI service returned an invalid response.")
        } finally {
            connection?.disconnect()
        }
    }

    override fun isAvailable(): Boolean = true
}

class LocalAIProvider : AIProvider {
    override suspend fun generateResponse(request: AIRequest): AIResponse = AIResponse(
        success = false,
        text = "",
        errorMessage = "No local AI model is configured.",
    )

    override fun isAvailable(): Boolean = false
}

class FreeCloudAIProvider : AIProvider {
    override suspend fun generateResponse(request: AIRequest): AIResponse = AIResponse(
        success = false,
        text = "",
        errorMessage = "No cloud AI provider is configured.",
    )

    override fun isAvailable(): Boolean = false
}
