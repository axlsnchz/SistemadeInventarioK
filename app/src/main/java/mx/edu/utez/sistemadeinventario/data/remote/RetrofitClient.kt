package mx.edu.utez.sistemadeinventario.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(tokenProvider: () -> String?) {

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
        }

        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    companion object {


        const val BASE_URL = "http://10.0.2.2:8080/"
        fun getImageUrl(fotoUrl: String?): String? {
        if (fotoUrl.isNullOrBlank()) return null
        if (fotoUrl.startsWith("http://") || fotoUrl.startsWith("https://")) return fotoUrl
        return BASE_URL.trimEnd('/') + "/" + fotoUrl.trimStart('/')
        }
    }
}
