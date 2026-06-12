package com.example.ubicafii.data.api

import com.example.ubicafii.data.model.Espacio
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/espacios")
    suspend fun getEspacios(
        @Query("piso") piso: Int? = null,
        @Query("tipo") tipo: String? = null,
        @Query("q") q: String? = null
    ): List<Espacio>
    @GET("api/espacios/{id}")
    suspend fun getEspacio(@Path("id") id: Int): Espacio

    @POST("api/espacios")
    suspend fun crearEspacio(@Body espacio: Espacio): Espacio

    @PUT("api/espacios/{id}")
    suspend fun actualizarEspacio(@Path("id") id: Int, @Body espacio: Espacio): Espacio

    @DELETE("api/espacios/{id}")
    suspend fun eliminarEspacio(@Path("id") id: Int)
}