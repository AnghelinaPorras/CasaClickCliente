package com.casaclick.cliente.data.remote
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.casaclick.cliente.data.remote.api.ApiService
import java.util.concurrent.TimeUnit
object RetrofitClient {
    fun crear(url: String, token: () -> String?): ApiService {
        val cliente = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).callTimeout(25, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                token()?.let { request.header("Authorization", "Bearer $it") }
                chain.proceed(request.build())
            }.build()
        return Retrofit.Builder().baseUrl(url).client(cliente).addConverterFactory(GsonConverterFactory.create()).build().create(ApiService::class.java)
    }
}
