package cl.ipfsoft.backend.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

/**
 * Almacena en local (DataStore) el token JWT y los datos de la sesión del
 * empleado autenticado.
 */
class TokenStore(private val context: Context) {

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("token")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_ROLE = stringPreferencesKey("role")
        val KEY_EMPLOYEE_ID = stringPreferencesKey("employee_id")
        val KEY_FULL_NAME = stringPreferencesKey("full_name")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    suspend fun saveSession(auth: AuthResponse) {
        context.dataStore.edit { prefs ->
            auth.token?.let { prefs[KEY_TOKEN] = it }
            auth.username?.let { prefs[KEY_USERNAME] = it }
            auth.role?.let { prefs[KEY_ROLE] = it }
            prefs[KEY_EMPLOYEE_ID] = auth.employeeId?.toString() ?: ""
            auth.fullName?.let { prefs[KEY_FULL_NAME] = it }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
