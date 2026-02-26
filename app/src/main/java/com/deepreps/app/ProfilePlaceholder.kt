package com.deepreps.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Temporary profile/settings screen placeholder until the Profile feature module is built.
 */
@Composable
fun ProfilePlaceholder() {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Profile & Settings",
            style = typography.headlineMedium,
            color = colors.onSurfacePrimary,
        )

        Text(
            text = "Coming soon",
            style = typography.bodyLarge,
            color = colors.onSurfaceTertiary,
            modifier = Modifier.padding(top = spacing.space2),
        )
    }
}
