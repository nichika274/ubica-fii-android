package com.example.ubicafii.data.api

import com.example.ubicafii.data.model.Espacio
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/espacios")
    suspend fun getEspacios(
        @Query("piso") piso: Int? = null,
        @Query("tipo") tipo: String? = null,
        @Query("q") query: String? = null
    ): List<Espacio>
}