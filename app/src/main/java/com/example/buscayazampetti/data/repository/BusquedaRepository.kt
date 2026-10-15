package com.example.buscayazampetti.data.repository

import com.example.buscayazampetti.data.model.SearchResult
import com.example.buscayazampetti.data.network.RetrofitClient

class BusquedaRepository {

    private val api = RetrofitClient.api

    private val searchUrl = "https://www.buscaya.com.ar/api/hybrid-search-android"

    suspend fun buscar(query: String): List<SearchResult> {
        return api.buscar(searchUrl, query).results
    }
}
