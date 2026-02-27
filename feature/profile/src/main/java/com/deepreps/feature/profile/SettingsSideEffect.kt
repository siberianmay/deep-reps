package com.deepreps.feature.profile

import com.deepreps.core.data.export.ImportResult
import java.io.File

/**
 * One-shot side effects emitted by [SettingsViewModel].
 * Consumed by the UI layer to trigger toasts, share intents, etc.
 */
sealed interface SettingsSideEffect {
    data class ExportReady(val file: File) : SettingsSideEffect
    data class ImportComplete(val result: ImportResult) : SettingsSideEffect
    data class ShowError(val message: String) : SettingsSideEffect
}
