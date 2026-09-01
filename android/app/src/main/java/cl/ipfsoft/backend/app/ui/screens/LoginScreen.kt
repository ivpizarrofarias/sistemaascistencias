package cl.ipfsoft.backend.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.ipfsoft.backend.app.ui.UiState

/**
 * Pantalla de inicio de sesión del empleado.
 */
@Composable
fun LoginScreen(
    state: UiState,
    onLogin: (String, String) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            Text("Portal del empleado", style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(28.dp))

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { onLogin(username, password) },
                enabled = username.isNotBlank() && password.isNotBlank() && !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.height(20.dp))
                } else {
                    Text("Ingresar")
                }
            }
        }
    }
}
