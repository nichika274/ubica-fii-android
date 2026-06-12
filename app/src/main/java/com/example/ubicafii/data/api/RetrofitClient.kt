package com.example.ubicafii.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 es la IP especial que usa el emulador de Android para conectarse al localhost de tu PC
    private const val BASE_URL = "http://10.0.2.2:3001/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}