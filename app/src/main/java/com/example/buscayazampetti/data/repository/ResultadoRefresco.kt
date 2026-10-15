package com.example.buscayazampetti.data.repository

/**
 * Cómo terminó un intento de traer datos frescos de la API.
 * "sealed" = estas son TODAS las opciones posibles; el "when" del
 * ViewModel avisa si te olvidás de alguna.
 */
sealed interface ResultadoRefresco {

    /** Se bajaron y guardaron [cantidad] farmacias. */
    data class Exito(val cantidad: Int) : ResultadoRefresco

    /** Ya había datos en Room y no se pidió forzar: no se llamó a la API. */
    data object SinCambios : ResultadoRefresco

    /** No hay internet (modo avión, timeout, servidor caído...). */
    data object SinConexion : ResultadoRefresco

    /** El servidor respondió, pero con un código de error (404, 500...). */
    data class ErrorServidor(val codigo: Int) : ResultadoRefresco

    /** Cualquier otra cosa (por ejemplo, un JSON con formato raro). */
    data class ErrorDesconocido(val mensaje: String?) : ResultadoRefresco
}
