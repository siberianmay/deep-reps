package com.deepreps.core.network.gemini.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generation configuration for Gemini API requests.
 *
 * Per architecture.md Section 4.4:
 * - Temperature: 0.3 (low creativity for consistent plans)
 * - TopP: 0.8
 * - Max output tokens: 2048
 * - Response MIME type: application/json (forces structured output)
 */
@Serializable
internal data class GeminiGenerationConfig(
    val temperature: Float = 0.3f,
    @SerialName("topP")
    val topP: Float = 0.8f,
    @SerialName("maxOutputTokens")
    val maxOutputTokens: Int = 2048,
    @SerialName("responseMimeType")
    val responseMimeType: String = "application/json",
)
