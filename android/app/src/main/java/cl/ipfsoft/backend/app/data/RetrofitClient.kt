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
 * La base URL apunta al backend LOCAL por defecto (el emulador ve el host con
 * 10.0.2.2). Las rutas de ApiService ya incluyen el prefijo "api/", por eso
 * BASE_URL termina solo en "/". Para un dispositivo físico cambiar por la IP
 * de la red local (p. ej. http://192.168.1.10:8080/).
 */
object RetrofitClient {

    // Backend local: http://10.0.2.2:8080/ es "localhost" desde el emulador.
    private const val BASE_URL = "http://10.0.2.2:8080/"

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
