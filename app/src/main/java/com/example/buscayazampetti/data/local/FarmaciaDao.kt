package com.example.buscayazampetti.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO = "Data Access Object": acá van TODAS las consultas a la tabla farmacias.
 * Room escribe la implementación por nosotros (la genera KSP al compilar)
 * y revisa el SQL: si escribís mal una columna, el proyecto NO compila.
 *
 * Regla general:
 *  - fun ...: Flow<...>  -> consulta "observable": emite de nuevo cada vez
 *                           que la tabla cambia. No lleva suspend.
 *  - suspend fun ...     -> operación de una sola vez (insertar, borrar,
 *                           contar). Room la ejecuta fuera del hilo principal.
 */
@Dao
interface FarmaciaDao {

    // Ordenamos por fecha del turno y después por nombre.
    @Query("SELECT * FROM farmacias ORDER BY fecha_iso ASC, nombre ASC")
    fun obtenerTodos(): Flow<List<FarmaciaEntity>>

    // REPLACE: si ya existe una fila con ese id, la pisa con la nueva.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(farmacias: List<FarmaciaEntity>)

    // Busca la fila por la clave primaria (id) y la actualiza.
    // Devuelve cuántas filas cambió (0 si no existía).
    @Update
    suspend fun actualizar(farmacia: FarmaciaEntity): Int

    // Borra la fila con ese id. Devuelve cuántas filas borró.
    @Delete
    suspend fun eliminar(farmacia: FarmaciaEntity): Int

    // BUSCADOR: consulta PARAMETRIZADA con LIKE.
    // :texto NUNCA se pega como texto dentro del SQL: Room lo pasa como
    // parámetro de SQLite, así que no hay riesgo de inyección SQL.
    // Los % se concatenan con || DENTRO de SQLite ("contiene el texto").
    // Si alguien escribe ' OR '1'='1, Room lo busca como texto literal.
    // Nota: en SQLite, LIKE no distingue mayúsculas de minúsculas (para letras
    // sin tilde), así que "central" encuentra "Farmacia Central".
    @Query(
        "SELECT * FROM farmacias " +
            "WHERE nombre LIKE '%' || :texto || '%' " +
            "ORDER BY fecha_iso ASC, nombre ASC"
    )
    fun buscarPorNombre(texto: String): Flow<List<FarmaciaEntity>>

    @Query("SELECT COUNT(*) FROM farmacias")
    suspend fun contar(): Int

    @Query("DELETE FROM farmacias")
    suspend fun borrarTodos()
}
