package com.deepreps.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiApiKey

/**
 * Hilt module providing the configured Ktor HttpClient for Gemini API communication.
 *
 * Per architecture.md Section 4.2:
 * - OkHttp engine (standard Android HTTP stack)
 * - ContentNegotiation with kotlinx.serialization JSON
 * - Logging at INFO level (no body logging in release builds)
 * - 30s request timeout, 10s connect timeout
 * - Base URL: generativelanguage.googleapis.com
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
            connectTimeoutMillis = CONNECT_TIMEOUT_MS
        }

        defaultRequest {
            url(BASE_URL)
        }
    }

    /**
     * API key provided via BuildConfig.
     *
     * The actual key is injected from the app module's build.gradle.kts:
     * ```
     * buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("geminiApiKey") ?: ""}\"")
     * ```
     *
     * For local development, set geminiApiKey in local.properties (gitignored).
     * For CI, inject via environment variable.
     */
    @Provides
    @GeminiApiKey
    fun provideGeminiApiKey(): String = com.deepreps.core.network.BuildConfig.GEMINI_API_KEY

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val REQUEST_TIMEOUT_MS = 30_000L
    private const val CONNECT_TIMEOUT_MS = 10_000L
}
