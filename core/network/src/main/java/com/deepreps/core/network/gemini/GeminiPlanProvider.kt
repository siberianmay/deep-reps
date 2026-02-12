package com.deepreps.core.network.gemini

import com.deepreps.core.domain.model.GeneratedPlan
import com.deepreps.core.domain.model.PlanRequest
import com.deepreps.core.domain.provider.AiPlanException
import com.deepreps.core.domain.provider.AiPlanProvider
import com.deepreps.core.network.di.GeminiApiKey
import com.deepreps.core.network.gemini.model.GeminiContent
import com.deepreps.core.network.gemini.model.GeminiGenerationConfig
import com.deepreps.core.network.gemini.model.GeminiPart
import com.deepreps.core.network.gemini.model.GeminiRequestBody
import com.deepreps.core.network.gemini.model.GeminiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import timber.log.Timber
import javax.inject.Inject

/**
 * Gemini API implementation of [AiPlanProvider].
 *
 * Per architecture.md Section 4.2:
 * - Sends prompt to Gemini 2.0 Flash via generateContent endpoint
 * - Parses response JSON into [GeneratedPlan]
 * - Error handling: network errors, API errors, parsing errors
 *
 * This is the only class in the codebase that knows about the Gemini API.
 * Swapping to another LLM means replacing this class and updating the Hilt binding.
 */
class GeminiPlanProvider @Inject constructor(
    private val httpClient: HttpClient,
    private val promptBuilder: GeminiPromptBuilder,
    private val responseParser: GeminiResponseParser,
    @GeminiApiKey private val apiKey: String,
) : AiPlanProvider {

    override suspend fun generatePlan(request: PlanRequest): GeneratedPlan {
        val prompt = promptBuilder.build(request)

        Timber.d("Gemini prompt built: %d chars, ~%d tokens", prompt.length, prompt.length / 4)

        val geminiResponse = try {
            httpClient.post(ENDPOINT) {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiRequestBody(
                        contents = listOf(
                            GeminiContent(parts = listOf(GeminiPart(text = prompt))),
                        ),
                        generationConfig = GeminiGenerationConfig(),
                    ),
                )
            }
        } catch (e: HttpRequestTimeoutException) {
            throw AiPlanException("Gemini API timeout", e)
        } catch (e: ClientRequestException) {
            throw AiPlanException("Gemini API error: ${e.response.status}", e)
        } catch (e: Exception) {
            throw AiPlanException("Unexpected error calling Gemini API: ${e.message}", e)
        }

        val body = try {
            geminiResponse.body<GeminiResponse>()
        } catch (e: Exception) {
            throw AiPlanException("Failed to deserialize Gemini response envelope: ${e.message}", e)
        }

        val text = body.candidates.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: throw AiPlanException("Empty response from Gemini: no candidates or text")

        Timber.d("Gemini response text length: %d chars", text.length)

        return responseParser.parse(text, request.exercises)
    }

    companion object {
        private const val ENDPOINT = "v1beta/models/gemini-2.0-flash:generateContent"
    }
}
