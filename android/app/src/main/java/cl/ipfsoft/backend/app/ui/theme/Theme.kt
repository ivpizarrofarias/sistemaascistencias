package cl.ipfsoft.backend.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de colores de la app (azul corporativo, similar al frontend web).
private val BluePrimary = Color(0xFF2563EB)
private val BlueDark = Color(0xFF1E3A8A)
private val Green = Color(0xFF16A34A)
private val Red = Color(0xFFDC2626)
private val Warning = Color(0xFFD97706)

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Green,
    onSecondary = Color.White,
    tertiary = Warning,
    error = Red,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF1E3A8A),
    secondary = Color(0xFF4ADE80),
    error = Color(0xFFF87171),
)

@Composable
fun SistemaAsistenciasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content,
    )
}
