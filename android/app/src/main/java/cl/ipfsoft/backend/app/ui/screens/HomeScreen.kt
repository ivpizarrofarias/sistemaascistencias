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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.ipfsoft.backend.app.data.AttendanceResponse
import cl.ipfsoft.backend.app.data.EmployeeResponse
import cl.ipfsoft.backend.app.data.LiquidacionResponse
import cl.ipfsoft.backend.app.data.PermisoLaboralResponse
import cl.ipfsoft.backend.app.ui.UiState

/**
 * Panel principal del empleado autenticado: consulta sus asistencias, permisos
 * y liquidaciones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: UiState,
    onLogout: () -> Unit,
    onSelectEmployee: (Long) -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Asistencia", "Permisos", "Liquidaciones")
    val colors = MaterialTheme.colorScheme
    val headerBrush = Brush.horizontalGradient(
        listOf(Color(0xFF006D77), Color(0xFF0FA3B1))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mi Portal", fontWeight = FontWeight.Bold)
                        Text(
                            state.fullName ?: state.username ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF006D77),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBrush)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    "Consulta tu información de forma rápida",
                    color = Color.White.copy(alpha = 0.95f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            EmployeeSelector(
                employees = state.employees,
                selectedId = state.employeeId,
                onSelect = onSelectEmployee,
            )

            TabRow(
                selectedTabIndex = tab,
                containerColor = colors.surface,
                contentColor = colors.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tab == index,
                        onClick = { tab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (tab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (tab) {
                0 -> AsistenciaTab(state.attendances)
                1 -> PermisosTab(state.permisos)
                2 -> LiquidacionesTab(state.liquidaciones)
            }
        }
    }
}

/** Selector visible para ADMIN/SUPERVISOR, que no tienen empleado propio. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeeSelector(
    employees: List<EmployeeResponse>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
) {
    if (employees.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    val seleccionado = employees.firstOrNull { it.id == selectedId } ?: employees.first()

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Empleado: ${nombreEmpleado(seleccionado)}")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            employees.forEach { emp ->
                DropdownMenuItem(
                    text = { Text(nombreEmpleado(emp)) },
                    onClick = {
                        onSelect(emp.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun nombreEmpleado(emp: EmployeeResponse): String =
    emp.fullName?.takeIf { it.isNotBlank() }
        ?: listOfNotNull(emp.firstName, emp.lastName).joinToString(" ").ifBlank { emp.rut ?: "Empleado ${emp.id}" }

@Composable
private fun AsistenciaTab(attendances: List<AttendanceResponse>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (attendances.isEmpty()) {
            item { EmptyCard("Sin registros de asistencia") }
            return@LazyColumn
        }
        items(attendances) { a ->
            InfoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(a.date, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    EstadoChip(
                        when {
                            a.absence -> "Ausente"
                            a.medicalLicense -> "Licencia"
                            a.complete -> "Completo"
                            else -> "Incompleto"
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DatoMini("Entrada", a.entryTime?.take(5) ?: "-")
                    DatoMini("Salida", a.exitTime?.take(5) ?: "-")
                }
            }
        }
    }
}

@Composable
private fun PermisosTab(permisos: List<PermisoLaboralResponse>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (permisos.isEmpty()) {
            item { EmptyCard("Sin permisos solicitados") }
            return@LazyColumn
        }
        items(permisos) { p ->
            InfoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(p.date ?: "-", fontWeight = FontWeight.Bold)
                    EstadoChip(p.estado ?: "—")
                }
                Spacer(Modifier.height(6.dp))
                Text(tipoPermiso(p), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                if (!p.motivo.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(p.motivo ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun LiquidacionesTab(liquidaciones: List<LiquidacionResponse>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (liquidaciones.isEmpty()) {
            item { EmptyCard("Sin liquidaciones generadas") }
            return@LazyColumn
        }
        items(liquidaciones) { l ->
            InfoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Período ${l.periodMonth.toString().padStart(2, '0')}/${l.periodYear}",
                        fontWeight = FontWeight.Bold
                    )
                    EstadoChip(if (l.pagada) "Pagada" else "Pendiente")
                }
                Spacer(Modifier.height(8.dp))
                Text("Líquido a pagar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    moneda(l.liquidoAPagar),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun EmptyCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun EstadoChip(text: String) {
    val (bg, fg) = when (text.lowercase()) {
        "completo", "pagada", "aprobado" -> Color(0xFFE6FFF3) to Color(0xFF1E8E5A)
        "ausente", "rechazado" -> Color(0xFFFFECEC) to Color(0xFFD64545)
        "incompleto", "pendiente" -> Color(0xFFFFF4DE) to Color(0xFFB26A00)
        else -> Color(0xFFEAF2FF) to Color(0xFF2459C3)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, color = fg, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DatoMini(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun tipoPermiso(p: PermisoLaboralResponse): String {
    val cant = p.horas?.takeIf { p.tipo == "POR_HORA" }?.let { "${it}h" }
        ?: p.dias.takeIf { p.tipo == "POR_DIA" }?.let { "${it} día/s" }
        ?: ""
    return if (p.tipo == "POR_HORA") "Por horas ($cant)" else "Por día ($cant)"
}

private fun moneda(valor: Double?): String {
    if (valor == null) return "-"
    val formato = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "CL"))
    return formato.format(valor)
}
