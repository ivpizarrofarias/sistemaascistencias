package cl.ipfsoft.backend.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.ipfsoft.backend.app.data.AttendanceResponse
import cl.ipfsoft.backend.app.data.LiquidacionResponse
import cl.ipfsoft.backend.app.data.PermisoLaboralResponse
import cl.ipfsoft.backend.app.ui.UiState

/**
 * Panel principal del empleado autenticado: consulta sus asistencias, permisos
 * y liquidaciones mediante pestañas.
 */
@Composable
fun HomeScreen(
    state: UiState,
    onLogout: () -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bienvenido/a")
                        Text(
                            state.fullName ?: state.username ?: "",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) { Text("Salir") }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Pestañas
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { tab = 0 }) { Text("Asistencia", fontWeight = if (tab == 0) FontWeight.Bold else FontWeight.Normal) }
                TextButton(onClick = { tab = 1 }) { Text("Permisos", fontWeight = if (tab == 1) FontWeight.Bold else FontWeight.Normal) }
                TextButton(onClick = { tab = 2 }) { Text("Liquidaciones", fontWeight = if (tab == 2) FontWeight.Bold else FontWeight.Normal) }
            }
            HorizontalDivider()

            when (tab) {
                0 -> AsistenciaTab(state.attendances)
                1 -> PermisosTab(state.permisos)
                2 -> LiquidacionesTab(state.liquidaciones)
            }
        }
    }
}

@Composable
private fun AsistenciaTab(attendances: List<AttendanceResponse>) {
    LazyColumn(Modifier.padding(16.dp)) {
        if (attendances.isEmpty()) {
            item { Text("Sin registros de asistencia") }
            return@LazyColumn
        }
        items(attendances) { a ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(a.date, modifier = Modifier.weight(1f))
                Text(a.entryTime?.take(5) ?: "-")
                Spacer(Modifier.height(0.dp))
                Text(a.exitTime?.take(5) ?: "-", modifier = Modifier.padding(start = 12.dp))
                Spacer(Modifier.weight(1f))
                val estado = when {
                    a.absence -> "Ausente"
                    a.medicalLicense -> "Licencia"
                    a.complete -> "Completo"
                    else -> "Incompleto"
                }
                Text(estado, style = MaterialTheme.typography.labelMedium)
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun PermisosTab(permisos: List<PermisoLaboralResponse>) {
    LazyColumn(Modifier.padding(16.dp)) {
        if (permisos.isEmpty()) {
            item { Text("Sin permisos solicitados") }
            return@LazyColumn
        }
        items(permisos) { p ->
            Column(Modifier.padding(vertical = 8.dp)) {
                Text("${p.date} · ${tipoPermiso(p)}", fontWeight = FontWeight.Bold)
                Text(p.motivo)
                Text("Estado: ${p.estado}", style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun LiquidacionesTab(liquidaciones: List<LiquidacionResponse>) {
    LazyColumn(Modifier.padding(16.dp)) {
        if (liquidaciones.isEmpty()) {
            item { Text("Sin liquidaciones generadas") }
            return@LazyColumn
        }
        items(liquidaciones) { l ->
            Column(Modifier.padding(vertical = 8.dp)) {
                Text(
                    "Período ${l.periodMonth.toString().padStart(2, '0')}/${l.periodYear}",
                    fontWeight = FontWeight.Bold,
                )
                Text("Líquido a pagar: ${moneda(l.liquidoAPagar)}")
                Text("Estado: ${if (l.pagada) "Pagada" else "Pendiente"}", style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider()
        }
    }
}

private fun tipoPermiso(p: PermisoLaboralResponse): String =
    if (p.tipo == "POR_HORA") "Por horas (${p.horas} h)" else "Por día (${p.dias} día/s)"

private fun moneda(valor: Double): String {
    val formato = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "CL"))
    return formato.format(valor)
}
