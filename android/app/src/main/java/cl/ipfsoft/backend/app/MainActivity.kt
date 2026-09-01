package cl.ipfsoft.backend.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import cl.ipfsoft.backend.app.data.RetrofitClient
import cl.ipfsoft.backend.app.data.TokenStore
import cl.ipfsoft.backend.app.ui.MainViewModel
import cl.ipfsoft.backend.app.ui.screens.HomeScreen
import cl.ipfsoft.backend.app.ui.screens.LoginScreen
import cl.ipfsoft.backend.app.ui.screens.MarkScreen
import cl.ipfsoft.backend.app.ui.theme.SistemaAsistenciasTheme

class MainActivity : ComponentActivity() {

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(
                        api = RetrofitClient.instance,
                        tokenStore = TokenStore(applicationContext),
                    ) as T
                }
            },
        )[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.restoreSession()
        setContent {
            SistemaAsistenciasTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(viewModel)
                }
            }
        }
    }
}

/**
 * Raíz de la app: decide entre la pantalla de marcaje (pública), el login y el
 * panel del empleado según el estado de sesión. Incluye una barra inferior
 * para alternar entre el kiosco y el portal.
 */
@Composable
private fun AppRoot(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    var pantalla by remember { mutableStateOf("marcaje") } // "marcaje" | "login"

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isAuthenticated -> {
                    HomeScreen(
                        state = state,
                        onLogout = {
                            viewModel.logout()
                            pantalla = "marcaje"
                        },
                    )
                }
                pantalla == "login" -> {
                    LoginScreen(
                        state = state,
                        onLogin = { u, p -> viewModel.login(u, p) },
                    )
                }
                else -> {
                    MarkScreen(
                        state = state,
                        onMark = { viewModel.mark(it) },
                    )
                }
            }
        }

        // Barra inferior de navegación (oculta cuando hay sesión iniciada).
        if (!state.isAuthenticated) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { pantalla = "marcaje" },
                        enabled = pantalla != "marcaje",
                    ) {
                        Text("Marcaje (kiosco)")
                    }
                    TextButton(
                        onClick = { pantalla = "login" },
                        enabled = pantalla != "login",
                    ) {
                        Text(stringResource(R.string.ingresar))
                    }
                }
            }
        }
    }
}
