package com.deepreps.core.network.gemini.model

import kotlinx.serialization.Serializable

/**
 * Response from Gemini generateContent API endpoint.
 *
 * The text content containing the JSON plan is nested inside:
 * candidates[0].content.parts[0].text
 */
@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
)

@Serializable
data class GeminiCandidate(
    val content: GeminiCandidateContent? = null,
    val finishReason: String? = null,
)

@Serializable
data class GeminiCandidateContent(
    val parts: List<GeminiResponsePart> = emptyList(),
    val role: String? = null,
)

@Serializable
data class GeminiResponsePart(
    val text: String? = null,
)
