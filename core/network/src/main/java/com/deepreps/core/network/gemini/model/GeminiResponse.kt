package com.deepreps.core.network.gemini.model

import kotlinx.serialization.Serializable

/**
 * Response from Gemini generateContent API endpoint.
 *
 * The text content containing the JSON plan is nested inside:
 * candidates[0].content.parts[0].text
 */
@Serializable
internal data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
)

@Serializable
internal data class GeminiCandidate(
    val content: GeminiCandidateContent? = null,
    val finishReason: String? = null,
)

@Serializable
internal data class GeminiCandidateContent(
    val parts: List<GeminiResponsePart> = emptyList(),
    val role: String? = null,
)

@Serializable
internal data class GeminiResponsePart(
    val text: String? = null,
)
