package cl.ipfsoft.backend.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.environmentDataStore by preferencesDataStore(name = "environment")

/**
 * Guarda el entorno de backend elegido (local o producción) por separado de la
 * sesión, para que cerrar sesión no resetee la elección.
 */
class EnvironmentStore(private val context: Context) {

    private companion object {
        val KEY_ENVIRONMENT = stringPreferencesKey("backend_environment")
    }

    val environment: Flow<BackendEnvironment> = context.environmentDataStore.data.map { prefs ->
        val nombre = prefs[KEY_ENVIRONMENT] ?: BackendEnvironment.LOCAL.name
        runCatching { BackendEnvironment.valueOf(nombre) }
            .getOrDefault(BackendEnvironment.LOCAL)
            .takeIf { it.isConfigured }
            ?: BackendEnvironment.LOCAL
    }

    suspend fun current(): BackendEnvironment = environment.first()

    suspend fun save(environment: BackendEnvironment) {
        context.environmentDataStore.edit { prefs ->
            prefs[KEY_ENVIRONMENT] = environment.name
        }
    }
}
