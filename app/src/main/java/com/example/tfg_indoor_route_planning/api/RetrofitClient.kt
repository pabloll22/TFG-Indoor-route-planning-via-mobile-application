package com.example.tfg_indoor_route_planning.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val authInterceptor = Interceptor { chain ->
        val peticionOriginal = chain.request()

        // Sacamos el Token del usuario
        val token = UserSession.token

        // Creamos una nueva petición basándonos en la original ...
        val constructorPeticion = peticionOriginal.newBuilder()

        // ... y si el usuario tiene Token, se lo pegamos como una pegatina en la cabecera
        if (token.isNotEmpty()) {
            constructorPeticion.header("Authorization", "Bearer $token")
        }

        // Enviamos la petición ya modificada
        chain.proceed(constructorPeticion.build())
    }

    // CREAMOS EL CLIENTE HTTP Y LE ASIGNAMOS EL INTERCEPTOR
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()
    val apiService: MapApiService by lazy {
        Retrofit.Builder()
            .baseUrl(MapApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MapApiService::class.java)
    }
}