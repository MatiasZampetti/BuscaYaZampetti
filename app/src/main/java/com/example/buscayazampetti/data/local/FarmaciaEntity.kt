package com.example.buscayazampetti.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room = una TABLA de SQLite.
 * Cada propiedad es una columna y cada objeto es una fila.
 *
 * Está SEPARADA del DTO (FarmaciaApi): el DTO copia la forma del JSON de la
 * API y esta clase representa cómo guardamos los datos en el teléfono.
 * Si mañana la API cambia un nombre de campo, solo tocamos el DTO y el mapper.
 *
 * Tabla resultante (más o menos):
 *   CREATE TABLE farmacias (id TEXT PRIMARY KEY NOT NULL, nombre TEXT NOT NULL,
 *     direccion TEXT NOT NULL, imagen TEXT NOT NULL, fecha_iso TEXT NOT NULL,
 *     fecha_raw TEXT NOT NULL, dia_semana TEXT NOT NULL,
 *     fecha_actualizacion INTEGER NOT NULL)
 */
@Entity(tableName = "farmacias")
data class FarmaciaEntity(

    // La API no manda un id, así que lo armamos con la fecha del turno y el
    // nombre (por ejemplo "2026-10-08_Farmacia Central"). Una misma farmacia
    // puede estar de turno varios días, por eso la fecha forma parte del id.
    @PrimaryKey
    val id: String,

    val nombre: String,

    val direccion: String,

    val imagen: String,

    // Fecha del turno en formato "yyyy-MM-dd". Sirve para ordenar la lista.
    @ColumnInfo(name = "fecha_iso")
    val fechaIso: String,

    @ColumnInfo(name = "fecha_raw")
    val fechaRaw: String,

    @ColumnInfo(name = "dia_semana")
    val diaSemana: String,

    // Momento (en milisegundos) en que bajamos este dato de la API.
    // Sirve para saber qué tan "fresco" está lo que tenemos guardado.
    @ColumnInfo(name = "fecha_actualizacion")
    val fechaActualizacion: Long
)
