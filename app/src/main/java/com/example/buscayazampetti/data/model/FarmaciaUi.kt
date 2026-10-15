package com.example.buscayazampetti.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FarmaciaUi(
    val iniciales: String,
    val colorIndex: Int,
    val diaSemana: String,
    val dia: Int,
    val mes: String,
    val nombre: String,
    val direccion: String,
    val imagen: String,
    val esHoy: Boolean
)

private val meses = listOf("", "Ene", "Feb", "Mar", "Abr", "May", "Jun",
    "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

fun FarmaciaApi.toUiModel(): FarmaciaUi =
    crearFarmaciaUi(fechaIso, diaSemana, nombre, direccion, imagen)

/**
 * Arma el modelo de pantalla a partir de los datos crudos de una farmacia.
 * La usan tanto el DTO de la API como la entidad de Room (ver FarmaciaMappers.kt),
 * así la lógica de iniciales, día, mes y "Hoy" está en un solo lugar.
 */
fun crearFarmaciaUi(
    fechaIso: String,
    diaSemana: String,
    nombre: String,
    direccion: String,
    imagen: String
): FarmaciaUi {
    val partes = fechaIso.split("-")
    val dia    = partes.getOrNull(2)?.toIntOrNull() ?: 0
    val mesNum = partes.getOrNull(1)?.toIntOrNull() ?: 0

    val iniciales = nombre
        .removePrefix("Farmacia").trim()
        .split(" ")
        .filter { it.isNotBlank() && it.first().isLetter() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifEmpty { nombre.take(2).uppercase() }

    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    return FarmaciaUi(
        iniciales  = iniciales,
        colorIndex = Math.abs(nombre.hashCode()) % 5,
        diaSemana  = diaSemana,
        dia        = dia,
        mes        = meses.getOrElse(mesNum) { "" },
        nombre     = nombre,
        direccion  = direccion,
        imagen     = imagen,
        esHoy      = fechaIso == hoy
    )
}
