package cl.ipfsoft.backend.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
        // Mantener la pantalla activa mientras la app esté en ejecución.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
 * Raíz de la app: decide entre el marcaje de asistencia y el panel del empleado
 * según el estado de sesión.
 */
@Composable
private fun AppRoot(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isAuthenticated -> {
                    HomeScreen(
                        state = state,
                        onLogout = { viewModel.logout() },
                        onSelectEmployee = { viewModel.selectEmployee(it) },
                    )
                }
                state.showLogin -> {
                    LoginScreen(
                        state = state,
                        onLogin = { u, p -> viewModel.login(u, p) },
                        onBack = { viewModel.openKiosk() },
                    )
                }
                else -> {
                    MarkScreen(
                        state = state,
                        onMark = { viewModel.mark(it) },
                        onOpenLogin = { viewModel.openLogin() },
                    )
                }
            }
        }
    }
}
