package cl.ipfsoft.backend.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006D77),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF83E8E0),
    onPrimaryContainer = Color(0xFF00201F),
    secondary = Color(0xFFFF6B35),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF6C63FF),
    background = Color(0xFFF4FBFA),
    onBackground = Color(0xFF102A2C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF102A2C),
    surfaceVariant = Color(0xFFE3F3F1),
    onSurfaceVariant = Color(0xFF3E5C5E),
    error = Color(0xFFD64545),
    onError = Color(0xFFFFFFFF),
)

@Composable
fun SistemaAsistenciasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
