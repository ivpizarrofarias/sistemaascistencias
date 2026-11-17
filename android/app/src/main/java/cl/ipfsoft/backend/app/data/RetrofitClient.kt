package cl.ipfsoft.backend.app.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP de la app mediante Retrofit/OkHttp.
 *
 * La base URL apunta a "10.0.2.2" que, dentro del emulador de Android, representa
 * el "localhost" de la máquina donde corre el backend Spring Boot (puerto 8080).
 * Para un dispositivo físico, cambiar BASE_URL a la IP local de la máquina.
 */
object RetrofitClient {

    // localhost visto desde el emulador Android.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // No registrar cuerpos/cabeceras en producción para evitar exponer tokens o datos sensibles.
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
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
