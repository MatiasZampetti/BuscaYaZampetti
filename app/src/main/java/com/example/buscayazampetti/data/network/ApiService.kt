package com.example.buscayazampetti.data.network

import com.example.buscayazampetti.data.model.FarmaciaResponse
import com.example.buscayazampetti.data.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {

    @GET("farmacias_turno_android")
    suspend fun getFarmacias(): FarmaciaResponse

    @GET
    suspend fun buscar(
        @Url url: String,
        @Query("q") query: String
    ): SearchResponse
}
