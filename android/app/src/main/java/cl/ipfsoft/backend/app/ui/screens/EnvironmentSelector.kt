package cl.ipfsoft.backend.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.ipfsoft.backend.app.data.BackendEnvironment

/**
 * Selector de backend (local / producción). La opción de producción aparece
 * deshabilitada si su URL no fue configurada en android/local.properties.
 */
@Composable
fun EnvironmentSelector(
    current: BackendEnvironment,
    onSelect: (BackendEnvironment) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BackendEnvironment.entries.forEach { environment ->
            val selected = current == environment
            Button(
                onClick = { onSelect(environment) },
                enabled = environment.isConfigured,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) colors.primary else colors.surfaceVariant,
                    contentColor = if (selected) colors.onPrimary else colors.onSurfaceVariant,
                    disabledContainerColor = colors.surfaceVariant.copy(alpha = 0.5f),
                    disabledContentColor = colors.onSurfaceVariant.copy(alpha = 0.6f),
                ),
            ) {
                Text(
                    text = if (environment.isConfigured) {
                        environment.label
                    } else {
                        "${environment.label} (sin configurar)"
                    },
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}
