package com.naveenai.app.ai

import android.util.Log
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
        val endpoint = "${baseUrl.trimEnd('/')}/api/chat"
        try {
            Log.d(TAG, "request starting: endpoint=${sanitizeEndpoint(endpoint)}, model=$model")
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
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
            Log.d(TAG, "HTTP response code: $responseCode")
            val responseBody = (if (responseCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            Log.d(TAG, "response received: bodyLength=${responseBody.length}")
            if (responseCode !in 200..299) {
                val modelError = runCatching { JSONObject(responseBody).optString("error") }.getOrNull()
                Log.e(TAG, "Ollama HTTP failure: errorPresent=${!modelError.isNullOrBlank()}")
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
                Log.e(TAG, "parsing failed: message.content is empty")
                AIResponse(false, "", "The local AI service returned an empty response.")
            } else {
                Log.d(TAG, "parsing success: responseTextPresent=true")
                AIResponse(true, text)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IOException) {
            Log.e(TAG, "request exception: ${exception::class.java.simpleName}: ${exception.message}")
            AIResponse(false, "", "I couldn't reach the local AI service. Start Ollama and try again.")
        } catch (exception: Exception) {
            Log.e(TAG, "request exception: ${exception::class.java.simpleName}: ${exception.message}")
            AIResponse(false, "", "The local AI service returned an invalid response.")
        } finally {
            connection?.disconnect()
        }
    }

    override fun isAvailable(): Boolean = true

    private companion object {
        const val TAG = "NAVEEN_OLLAMA"
        const val CONNECT_TIMEOUT_MS = 30_000
        const val READ_TIMEOUT_MS = 300_000

        fun sanitizeEndpoint(endpoint: String): String = runCatching {
            val parsed = URL(endpoint)
            "${parsed.protocol}://${parsed.host}:${parsed.port}${parsed.path}"
        }.getOrDefault("<invalid-endpoint>")
    }
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
