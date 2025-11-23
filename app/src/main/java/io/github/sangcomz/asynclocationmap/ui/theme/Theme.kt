package io.github.sangcomz.asynclocationmap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme()

/**
 * AsyncLocationMap Theme
 *
 * Material 3 테마를 적용합니다.
 */
@Composable
fun AsyncLocationMapTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
