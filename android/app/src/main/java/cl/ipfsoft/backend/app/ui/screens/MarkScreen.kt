package cl.ipfsoft.backend.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.ipfsoft.backend.app.data.BackendEnvironment
import cl.ipfsoft.backend.app.data.MarkResponse
import cl.ipfsoft.backend.app.ui.UiState

/**
 * Pantalla de marcaje de asistencia.
 * Pública: el trabajador digita su código de marcaje y registra el siguiente paso del día.
 */
@Composable
fun MarkScreen(
    state: UiState,
    onMark: (String) -> Unit,
    onOpenLogin: () -> Unit,
    onSelectEnvironment: (BackendEnvironment) -> Unit,
) {
    var codigo by remember { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF006D77), Color(0xFF0FA3B1), Color(0xFF83E8E0))
    )

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Spacer(Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🕒", fontSize = 42.sp)
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Marcaje de Asistencia",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "¡Registra tu marcaje en segundos!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.96f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text(
                            "Entorno del backend",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        EnvironmentSelector(
                            current = state.environment,
                            onSelect = onSelectEnvironment,
                        )
                        Spacer(Modifier.height(20.dp))

                        Text(
                            "Ingresa tu código de marcaje",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            "Marca tu entrada, colación o salida de forma rápida y segura",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )

                        Spacer(Modifier.height(16.dp))

                        state.error?.let {
                            StatusBanner(
                                text = it,
                                bg = Color(0xFFFFECEC),
                                fg = colors.error
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        state.markResult?.let { mostrarResultado(it) }

                        OutlinedTextField(
                            value = codigo,
                            onValueChange = { input ->
                                // Solo se aceptan dígitos, como máximo 4.
                                codigo = input.filter { it.isDigit() }.take(4)
                            },
                            label = { Text("Código de marcaje") },
                            placeholder = { Text("Ej: 4832") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                capitalization = KeyboardCapitalization.None,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.surfaceVariant,
                                focusedContainerColor = colors.surfaceVariant.copy(alpha = 0.35f),
                                unfocusedContainerColor = colors.surfaceVariant.copy(alpha = 0.2f)
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { onMark(codigo) },
                            enabled = codigo.length == 4 && !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.secondary,
                                contentColor = colors.onSecondary,
                                disabledContainerColor = colors.surfaceVariant
                            )
                        ) {
                            Text(
                                if (state.isLoading) "Marcando asistencia..." else "Marcar asistencia",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    "Usa tu código personal para realizar tu marcaje de asistencia",
                    color = Color.White.copy(alpha = 0.94f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                TextButton(onClick = onOpenLogin) {
                    Text(
                        "Portal del empleado (iniciar sesión)",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/** Muestra el resultado/estado de la marca actual. */
@Composable
private fun mostrarResultado(resp: MarkResponse) {
    val ok = !resp.blocked && !resp.complete && resp.nextStep != null
    val bg = when {
        resp.blocked -> Color(0xFFFFECEC)
        resp.complete -> Color(0xFFE6FFF3)
        ok -> Color(0xFFEAF7FF)
        else -> Color(0xFFF3F6F8)
    }
    val fg = when {
        resp.blocked -> Color(0xFFD64545)
        resp.complete -> Color(0xFF1E8E5A)
        ok -> Color(0xFF006D77)
        else -> Color(0xFF334155)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(14.dp)
    ) {
        resp.fullName?.let {
            Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = fg)
            Spacer(Modifier.height(6.dp))
        }
        when {
            resp.blocked -> Text(resp.message ?: "Registro bloqueado", color = fg, fontWeight = FontWeight.Medium)
            resp.complete -> Text(resp.message ?: "Jornada completa", color = fg, fontWeight = FontWeight.Medium)
            resp.nextStep != null -> {
                Text("Siguiente paso: ${etiquetaPaso(resp.nextStep)}", fontWeight = FontWeight.SemiBold, color = fg)
                Spacer(Modifier.height(8.dp))
                MarcasGrid(resp)
            }
            resp.message != null -> Text(resp.message, color = fg)
        }
    }
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun MarcasGrid(resp: MarkResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        textoMarca("Entrada", resp.entryTime)
        textoMarca("Salida a colación", resp.colacionSalida1)
        textoMarca("Entrada de colación", resp.colacionEntrada1)
        textoMarca("Salida a colación", resp.colacionSalida2)
        textoMarca("Entrada de colación", resp.colacionEntrada2)
        textoMarca("Salida", resp.exitTime)
    }
}

@Composable
private fun textoMarca(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value?.take(5) ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (value != null) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatusBanner(text: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(12.dp)
    ) {
        Text(text, color = fg, fontWeight = FontWeight.Medium)
    }
}

private fun etiquetaPaso(paso: String): String = when (paso) {
    "ENTRADA" -> "Entrada"
    "COLACION_SALIDA" -> "Salida a colación"
    "COLACION_ENTRADA" -> "Entrada de colación"
    "SALIDA" -> "Salida"
    "COMPLETE" -> "Completo"
    else -> paso
}
