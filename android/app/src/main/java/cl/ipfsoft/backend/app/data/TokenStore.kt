package cl.ipfsoft.backend.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para obtener el DataStore de preferencias de forma singleton por proceso.
private val Context.dataStore by preferencesDataStore(name = "auth_store")

/**
 * Guarda y recupera el token JWT y los datos del usuario autenticado.
 */
class TokenStore(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USERNAME = stringPreferencesKey("username")
        val ROLE = stringPreferencesKey("role")
        val EMPLOYEE_ID = stringPreferencesKey("employee_id")
        val FULL_NAME = stringPreferencesKey("full_name")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }

    /** Lee la sesión completa guardada (o null si no hay sesión). */
    val session: Flow<AuthResponse?> = context.dataStore.data.map { prefs ->
        val t = prefs[Keys.TOKEN] ?: return@map null
        AuthResponse(
            token = t,
            username = prefs[Keys.USERNAME] ?: "",
            role = prefs[Keys.ROLE] ?: "",
            employeeId = prefs[Keys.EMPLOYEE_ID]?.toLongOrNull(),
            fullName = prefs[Keys.FULL_NAME],
        )
    }

    suspend fun saveSession(auth: AuthResponse) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = auth.token
            prefs[Keys.USERNAME] = auth.username
            prefs[Keys.ROLE] = auth.role
            auth.employeeId?.let { prefs[Keys.EMPLOYEE_ID] = it.toString() }
            auth.fullName?.let { prefs[Keys.FULL_NAME] = it }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
