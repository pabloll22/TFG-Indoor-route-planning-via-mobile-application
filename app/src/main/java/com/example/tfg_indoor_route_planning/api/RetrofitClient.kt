package com.example.tfg_indoor_route_planning.api

import com.example.tfg_indoor_route_planning.api.services.AuthApiService
import com.example.tfg_indoor_route_planning.api.services.HorarioApiService
import com.example.tfg_indoor_route_planning.api.services.MapApiService
import com.example.tfg_indoor_route_planning.api.services.UsuarioApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // const val BASE_URL = "http://192.168.137.1:3000"
    const val BASE_URL = "http://192.168.1.49:3000/"

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
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val usuarioService: UsuarioApiService by lazy {
        retrofit.create(UsuarioApiService::class.java)
    }

    val horarioService: HorarioApiService by lazy {
        retrofit.create(HorarioApiService::class.java)
    }

    val mapaService: MapApiService by lazy {
        retrofit.create(MapApiService::class.java)
    }
}