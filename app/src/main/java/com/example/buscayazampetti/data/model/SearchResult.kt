package com.example.buscayazampetti.data.model

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    val results: List<SearchResult>,
    val total: Int,
    @SerializedName("local_count")  val localCount: Int,
    @SerializedName("google_count") val googleCount: Int,
    val location: String,
    val message: String
)

data class SearchResult(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: String,
    val imagen: String,
    @SerializedName("negocio_nombre")  val negocioNombre: String,
    @SerializedName("email_contacto") val emailContacto: String,
    val telefono: String,
    val fuente: String,
    val ubicacion: String,
    val rating: Float,
    @SerializedName("place_id") val placeId: String
)
