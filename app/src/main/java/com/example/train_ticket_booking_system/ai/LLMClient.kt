package com.example.train_ticket_booking_system.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class LLMMessage(val role: String, val content: String?, val toolCalls: List<ToolCall>? = null, val toolCallId: String? = null)
data class ToolCall(val id: String, val name: String, val arguments: String)
data class LLMResponse(val content: String?, val toolCalls: List<ToolCall>?)

class LLMClient {
    suspend fun chat(
        apiUrl: String, apiKey: String, model: String,
        messages: List<LLMMessage>, tools: List<JSONObject>? = null
    ): LLMResponse = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                messages.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", msg.role)
                        if (msg.content != null) put("content", msg.content)
                        if (msg.toolCallId != null) put("tool_call_id", msg.toolCallId)
                        if (msg.toolCalls != null) put("tool_calls", JSONArray().apply {
                            msg.toolCalls.forEach { tc ->
                                put(JSONObject().apply {
                                    put("id", tc.id); put("type", "function")
                                    put("function", JSONObject().apply { put("name", tc.name); put("arguments", tc.arguments) })
                                })
                            }
                        })
                    })
                }
            })
            if (tools != null) { put("tools", JSONArray().apply { tools.forEach { put(it) } }); put("tool_choice", "auto") }
        }

        val url = URL("$apiUrl/chat/completions")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
            connectTimeout = 30000; readTimeout = 60000
        }
        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

        val responseBody = if (conn.responseCode in 200..299) {
            BufferedReader(InputStreamReader(conn.inputStream)).readText()
        } else {
            val err = BufferedReader(InputStreamReader(conn.errorStream)).readText()
            throw Exception("API error ${conn.responseCode}: $err")
        }

        val json = JSONObject(responseBody)
        val choice = json.getJSONArray("choices").getJSONObject(0)
        val message = choice.getJSONObject("message")
        val content = message.optString("content", "").takeIf { it.isNotEmpty() }

        val toolCalls = if (message.has("tool_calls")) {
            message.getJSONArray("tool_calls").let { arr ->
                (0 until arr.length()).map { i ->
                    val tc = arr.getJSONObject(i)
                    ToolCall(tc.getString("id"), tc.getJSONObject("function").getString("name"),
                        tc.getJSONObject("function").getString("arguments"))
                }
            }
        } else null

        LLMResponse(content, toolCalls)
    }
}
