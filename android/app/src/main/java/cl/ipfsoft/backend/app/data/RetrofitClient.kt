package cl.ipfsoft.backend.app.data

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP de la app mediante Retrofit/OkHttp.
 *
 * La base URL apunta al despliegue Docker en el servidor propio. Las rutas de
 * ApiService ya incluyen el prefijo "api/", por eso BASE_URL termina solo en "/".
 */
object RetrofitClient {

    // Servidor propio: nginx publica el frontend y el proxy /api en el puerto 8081.
    private const val BASE_URL = "http://100.73.41.33:8081/"

    // No registrar cuerpos/cabeceras en producción para evitar exponer tokens o datos sensibles.
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

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
