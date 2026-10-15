package com.example.buscayazampetti.data.repository

import androidx.room.withTransaction
import com.example.buscayazampetti.data.local.BuscaYaDatabase
import com.example.buscayazampetti.data.local.toEntity
import com.example.buscayazampetti.data.local.toUiModel
import com.example.buscayazampetti.data.model.FarmaciaUi
import com.example.buscayazampetti.data.network.ApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

/**
 * El repositorio decide DE DÓNDE salen los datos.
 * El ViewModel y la pantalla no saben si vienen de Room o de la API.
 *
 * Estrategia "cache-first" con Room como ÚNICA FUENTE DE VERDAD:
 *   1. La UI siempre observa Room (los Flow de abajo).
 *   2. La API solo sirve para LLENAR o ACTUALIZAR Room.
 *   3. Cuando Room cambia, el Flow emite solo y la lista se redibuja.
 */
class FarmaciaRepository(
    private val db: BuscaYaDatabase,
    private val api: ApiService
) {

    private val dao = db.farmaciaDao()

    /** Todas las farmacias, siempre leídas desde Room. */
    val farmacias: Flow<List<FarmaciaUi>> =
        dao.obtenerTodos().map { lista -> lista.map { it.toUiModel() } }

    /**
     * Buscador: filtra EN LA BASE (consulta con LIKE en el DAO),
     * no en una lista en memoria. Como devuelve un Flow, si la tabla
     * cambia mientras el usuario busca, los resultados se actualizan solos.
     */
    fun buscar(texto: String): Flow<List<FarmaciaUi>> =
        dao.buscarPorNombre(texto.trim())
            .map { lista -> lista.map { it.toUiModel() } }

    /**
     * Trae datos frescos de la API y los guarda en Room.
     *
     * @param forzar false = solo va a la API si Room está vacío.
     *               true  = va igual (botón "Actualizar").
     *
     * No hace falta withContext(Dispatchers.IO): las funciones suspend de
     * Room y de Retrofit ya trabajan fuera del hilo principal.
     */
    suspend fun refrescar(forzar: Boolean = false): ResultadoRefresco {
        val hayDatosLocales = dao.contar() > 0
        if (hayDatosLocales && !forzar) return ResultadoRefresco.SinCambios

        return try {
            val dtos = api.getFarmacias().farmacias          // red
            val ahora = System.currentTimeMillis()
            val entidades = dtos.map { it.toEntity(ahora) }

            // Transacción: borrar + insertar es "todo o nada".
            // Si algo falla en el medio, Room deshace todo y la UI nunca ve
            // la tabla vacía a mitad de camino. Borramos antes para que
            // desaparezcan los turnos viejos que la API ya no manda.
            db.withTransaction {
                dao.borrarTodos()
                dao.insertarTodos(entidades)
            }
            ResultadoRefresco.Exito(entidades.size)

        } catch (e: IOException) {
            // Sin internet, modo avión, timeout, servidor apagado...
            // Room NO se toca: la UI sigue mostrando lo guardado.
            ResultadoRefresco.SinConexion
        } catch (e: HttpException) {
            // El servidor contestó con un código de error (4xx / 5xx).
            ResultadoRefresco.ErrorServidor(e.code())
        } catch (e: CancellationException) {
            // Nunca "tragarse" la cancelación de una corrutina
            // (por ejemplo, si el usuario cerró la pantalla).
            throw e
        } catch (e: Exception) {
            ResultadoRefresco.ErrorDesconocido(e.message)
        }
    }
}
