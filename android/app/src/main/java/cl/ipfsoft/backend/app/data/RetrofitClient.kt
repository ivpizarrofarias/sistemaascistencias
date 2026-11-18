package cl.ipfsoft.backend.app.data

import cl.ipfsoft.backend.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Backends que puede usar la app. En ambos casos se consume la API REST: la app
 * nunca se conecta a MySQL directamente.
 *
 * Las URLs se inyectan desde android/local.properties en tiempo de compilación,
 * por lo que la URL de producción no se sube a GitHub. Si no está configurada,
 * la opción aparece deshabilitada en la app.
 */
enum class BackendEnvironment(val label: String, val baseUrl: String) {
    LOCAL("Local", BuildConfig.LOCAL_BASE_URL),
    PRODUCCION("Producción", BuildConfig.PRODUCTION_BASE_URL),
    ;

    val isConfigured: Boolean
        get() = baseUrl.startsWith("http://") || baseUrl.startsWith("https://")
}

/**
 * Cliente HTTP de la app mediante Retrofit/OkHttp.
 *
 * Las rutas de ApiService ya incluyen el prefijo "api/", por eso las URLs base
 * terminan solo en "/". Se crea un cliente por entorno y se reutiliza.
 */
object RetrofitClient {

    // No registrar cuerpos/cabeceras para evitar exponer tokens o datos sensibles.
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    /**
     * Adjunta el JWT a todas las peticiones. Los endpoints públicos (login y
     * marcaje) ignoran la cabecera, así que es seguro enviarla siempre.
     */
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val token = TokenHolder.token
        val request: Request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val instances = mutableMapOf<BackendEnvironment, ApiService>()

    /** Devuelve el cliente API del entorno indicado (local o producción). */
    fun api(environment: BackendEnvironment): ApiService = synchronized(instances) {
        instances.getOrPut(environment) {
            require(environment.isConfigured) {
                "Falta configurar la URL del backend ${environment.label} en android/local.properties"
            }
            Retrofit.Builder()
                .baseUrl(environment.baseUrl)
                .client(okHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
