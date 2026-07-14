package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AiClient {
    private const val TAG = "AiClient"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun generateResponse(
        provider: String,
        apiKey: String,
        model: String,
        baseUrl: String,
        prompt: String,
        systemInstruction: String = "You are an expert Proxmox VE System Administrator AI Assistant. Help the user diagnose their cluster, write CLI commands, or answer their questions clearly."
    ): String = withContext(Dispatchers.IO) {
        try {
            when (provider) {
                "Gemini" -> callGemini(apiKey, model, prompt, systemInstruction)
                "OpenAI" -> callOpenAI(apiKey, model, baseUrl, prompt, systemInstruction)
                "Claude" -> callClaude(apiKey, model, prompt, systemInstruction)
                "Ollama" -> callOllama(model, baseUrl, prompt, systemInstruction)
                else -> "Unsupported AI provider: $provider"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating AI response", e)
            "Error: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
        }
    }

    private fun callGemini(
        apiKey: String,
        model: String,
        prompt: String,
        systemInstruction: String
    ): String {
        // Fallback to built-in BuildConfig key if user hasn't provided one
        val activeKey = apiKey.ifBlank { BuildConfig.GEMINI_API_KEY }
        if (activeKey.isBlank()) {
            return "Error: Gemini API Key is missing. Please configure it in Options/Settings."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$activeKey"

        val requestJson = JSONObject().apply {
            // System instructions
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            })
            // Contents
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }

        val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: return "Error: Empty response from Gemini API"
            if (!response.isSuccessful) {
                return "Error (HTTP ${response.code}): ${responseBody}"
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                if (content != null) {
                    val parts = content.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", "No text generated.")
                    }
                }
            }
            return "Error: No text generated. Raw response: $responseBody"
        }
    }

    private fun callOpenAI(
        apiKey: String,
        model: String,
        baseUrl: String,
        prompt: String,
        systemInstruction: String
    ): String {
        if (apiKey.isBlank()) {
            return "Error: OpenAI API Key is missing. Please configure it in Options/Settings."
        }

        val base = if (baseUrl.isBlank()) "https://api.openai.com" else baseUrl.trimEnd('/')
        val url = "$base/v1/chat/completions"

        val requestJson = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemInstruction)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: return "Error: Empty response from OpenAI API"
            if (!response.isSuccessful) {
                return "Error (HTTP ${response.code}): ${responseBody}"
            }

            val json = JSONObject(responseBody)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val choice = choices.getJSONObject(0)
                val message = choice.optJSONObject("message")
                if (message != null) {
                    return message.optString("content", "No text generated.")
                }
            }
            return "Error: No text generated. Raw response: $responseBody"
        }
    }

    private fun callClaude(
        apiKey: String,
        model: String,
        prompt: String,
        systemInstruction: String
    ): String {
        if (apiKey.isBlank()) {
            return "Error: Claude/Anthropic API Key is missing. Please configure it in Options/Settings."
        }

        val url = "https://api.anthropic.com/v1/messages"

        val requestJson = JSONObject().apply {
            put("model", model)
            put("max_tokens", 2048)
            put("system", systemInstruction)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: return "Error: Empty response from Claude API"
            if (!response.isSuccessful) {
                return "Error (HTTP ${response.code}): ${responseBody}"
            }

            val json = JSONObject(responseBody)
            val contentArray = json.optJSONArray("content")
            if (contentArray != null && contentArray.length() > 0) {
                val contentObj = contentArray.getJSONObject(0)
                return contentObj.optString("text", "No text generated.")
            }
            return "Error: No text generated. Raw response: $responseBody"
        }
    }

    private fun callOllama(
        model: String,
        baseUrl: String,
        prompt: String,
        systemInstruction: String
    ): String {
        val base = if (baseUrl.isBlank()) "http://10.0.2.2:11434" else baseUrl.trimEnd('/')
        val url = "$base/api/generate"

        val requestJson = JSONObject().apply {
            put("model", model)
            put("system", systemInstruction)
            put("prompt", prompt)
            put("stream", false)
        }

        val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: return "Error: Empty response from Ollama API"
            if (!response.isSuccessful) {
                return "Error (HTTP ${response.code}): ${responseBody}"
            }

            val json = JSONObject(responseBody)
            return json.optString("response", "No text generated.")
        }
    }
}
