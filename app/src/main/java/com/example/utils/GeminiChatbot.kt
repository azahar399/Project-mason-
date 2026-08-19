package com.example.utils

import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.firstOrNull
import com.example.data.* 

class GeminiChatbot(private val repository: AppRepository) {

    suspend fun processPremiumMessage(message: String, apiKey: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext "Error: API Key is missing. Please enter your Gemini API Key in the settings."
        }

        try {
            val flats = repository.allFlats.firstOrNull() ?: emptyList()
            val workColumns = repository.allWorkColumns.firstOrNull() ?: emptyList()
            val persons = repository.allMasons.firstOrNull() ?: emptyList()

            val flatsContext = flats.joinToString(", ") { "${it.id}:${it.name}" }
            val workColsContext = workColumns.joinToString(", ") { "${it.id}:${it.name}" }
            val personsContext = persons.joinToString(", ") { "${it.id}:${it.name}" }

            val systemInstruction = """
                You are a smart assistant for a construction work tracker app. 
                Your job is to understand the user's intent to log a work entry and extract the details.
                
                Available Flats (ID:Name): $flatsContext
                Available Work Categories (ID:Name): $workColsContext
                Available Personnel (ID:Name): $personsContext
                
                Analyze the user's message.
                If they are logging work, figure out the flatId, workColumnId, and optionally masonId.
                Return ONLY a JSON response in this exact format:
                {
                   "action": "log_work",
                   "flatId": 123,
                   "workColumnId": 456,
                   "masonId": 789,
                   "reply": "I have logged the Brick Work for Flat 101 by John."
                }
                If you cannot determine BOTH the flat and the work category, OR if the user is just asking a general question, return:
                {
                   "action": "chat",
                   "reply": "Your conversational response here."
                }
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = message)))),
                generationConfig = GenerationConfig(
                    responseFormat = ResponseFormat(text = ResponseFormatText(mimeType = "application/json"))
                ),
                systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
            )

            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (jsonText != null) {
                val jsonElement = Json.parseToJsonElement(jsonText).jsonObject
                val action = jsonElement["action"]?.jsonPrimitive?.content
                val reply = jsonElement["reply"]?.jsonPrimitive?.content ?: "Done."
                
                if (action == "log_work") {
                    val flatId = jsonElement["flatId"]?.jsonPrimitive?.intOrNull
                    val workColId = jsonElement["workColumnId"]?.jsonPrimitive?.intOrNull
                    val masonId = jsonElement["masonId"]?.jsonPrimitive?.intOrNull
                    
                    if (flatId != null && workColId != null) {
                        repository.insertWorkEntry(
                            WorkEntry(
                                flatId = flatId,
                                workColumnId = workColId,
                                masonId = masonId,
                                helperId = null,
                                date = System.currentTimeMillis()
                            )
                        )
                    }
                }
                return@withContext reply
            } else {
                return@withContext "Error: No valid response from Gemini."
            }

        } catch (e: Exception) {
            return@withContext "API Error: ${e.message}"
        }
    }
}
