package com.example.tfg_indoor_route_planning.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val apiService: MapApiService by lazy {
        Retrofit.Builder()
            .baseUrl(MapApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MapApiService::class.java)
    }
}