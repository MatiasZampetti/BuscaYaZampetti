package com.example.buscayazampetti.data.local

import com.example.buscayazampetti.data.model.FarmaciaApi
import com.example.buscayazampetti.data.model.FarmaciaUi
import com.example.buscayazampetti.data.model.crearFarmaciaUi

/*
 * Funciones de extensión que convierten entre las tres "formas" de una farmacia:
 *
 *   FarmaciaApi (DTO, API)  --toEntity()-->   FarmaciaEntity (Room)
 *   FarmaciaEntity (Room)   --toUiModel()-->  FarmaciaUi (pantalla)
 */

/**
 * Convierte lo que vino de la API en una fila para Room.
 *
 * @param ahora momento de la descarga, se guarda en fecha_actualizacion.
 */
fun FarmaciaApi.toEntity(ahora: Long): FarmaciaEntity = FarmaciaEntity(
    id = "${fechaIso}_${nombre}",
    nombre = nombre.trim(),
    direccion = direccion,
    imagen = imagen,
    fechaIso = fechaIso,
    fechaRaw = fechaRaw,
    diaSemana = diaSemana,
    fechaActualizacion = ahora
)

/** Convierte una fila de Room en el modelo que usa la pantalla. */
fun FarmaciaEntity.toUiModel(): FarmaciaUi = crearFarmaciaUi(
    fechaIso = fechaIso,
    diaSemana = diaSemana,
    nombre = nombre,
    direccion = direccion,
    imagen = imagen
)
