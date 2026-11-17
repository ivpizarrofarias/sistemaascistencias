package cl.ipfsoft.backend.app.data

/**
 * Token JWT en memoria para que el interceptor de OkHttp pueda adjuntarlo a
 * cada petición sin depender de un Context ni de operaciones suspendidas.
 *
 * Se llena al iniciar sesión o al restaurar la sesión guardada, y se limpia al
 * cerrar sesión. Es @Volatile porque el interceptor corre en otro hilo.
 */
object TokenHolder {
    @Volatile
    var token: String? = null
        private set

    fun set(token: String?) {
        this.token = token
    }

    fun clear() {
        token = null
    }
}
