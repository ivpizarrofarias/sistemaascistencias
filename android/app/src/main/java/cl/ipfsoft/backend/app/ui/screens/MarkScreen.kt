package cl.ipfsoft.backend.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.ipfsoft.backend.app.ui.UiState
import cl.ipfsoft.backend.app.data.MarkResponse

/**
 * Pantalla de marcaje (kiosco).
 * Pública: el trabajador digita su RUT y marca el siguiente paso del día.
 */
@Composable
fun MarkScreen(
    state: UiState,
    onMark: (String) -> Unit,
) {
    var rut by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        containerColor = MaterialTheme.colorScheme.primary,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Sistema de Asistencias", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Marcaje de asistencia (kiosco)", color = MaterialTheme.colorScheme.onPrimary)

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(20.dp)) {
                    state.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                    }

                    state.markResult?.let { mostrarResultado(it) }

                    OutlinedTextField(
                        value = rut,
                        onValueChange = { rut = it },
                        label = { Text("RUT del trabajador") },
                        placeholder = { Text("12.345.678-5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.None,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onMark(rut) },
                            enabled = rut.isNotBlank() && !state.isLoading,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (state.isLoading) "Registrando..." else "Marcar")
                        }
                    }
                }
            }
        }
    }
}

/** Muestra el resultado/estado de la marca actual. */
@Composable
private fun mostrarResultado(resp: MarkResponse) {
    Column {
        resp.fullName?.let {
            Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        when {
            resp.blocked -> Text(resp.message ?: "Bloqueado", color = MaterialTheme.colorScheme.error)
            resp.complete -> Text(resp.message ?: "Jornada completa", color = MaterialTheme.colorScheme.primary)
            resp.nextStep != null -> {
                Text("Siguiente paso: ${etiquetaPaso(resp.nextStep)}", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                textoMarca("Entrada", resp.entryTime)
                textoMarca("Salida colación 1", resp.colacionSalida1)
                textoMarca("Entrada colación 1", resp.colacionEntrada1)
                textoMarca("Salida colación 2", resp.colacionSalida2)
                textoMarca("Entrada colación 2", resp.colacionEntrada2)
                textoMarca("Salida", resp.exitTime)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun textoMarca(label: String, value: String?) {
    if (value != null) {
        Text("$label: ${value.take(5)}", style = MaterialTheme.typography.bodyMedium)
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
