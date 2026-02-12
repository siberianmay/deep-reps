package com.deepreps.core.network.gemini.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for the Gemini generateContent API endpoint.
 *
 * Per architecture.md Section 4.2:
 * POST v1beta/models/gemini-2.0-flash:generateContent
 */
@Serializable
data class GeminiRequestBody(
    val contents: List<GeminiContent>,
    @SerialName("generationConfig")
    val generationConfig: GeminiGenerationConfig,
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user",
)

@Serializable
data class GeminiPart(
    val text: String,
)
