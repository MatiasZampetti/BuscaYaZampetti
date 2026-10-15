package com.example.buscayazampetti.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.buscayazampetti.data.local.BuscaYaDatabase
import com.example.buscayazampetti.data.model.FarmaciaUi
import com.example.buscayazampetti.data.model.SearchResult
import com.example.buscayazampetti.data.network.RetrofitClient
import com.example.buscayazampetti.data.repository.BusquedaRepository
import com.example.buscayazampetti.data.repository.FarmaciaRepository
import com.example.buscayazampetti.data.repository.ResultadoRefresco
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Cargando : SearchUiState()
    data class Exito(val results: List<SearchResult>) : SearchUiState()
    data class Error(val mensaje: String) : SearchUiState()
}

/**
 * TODO lo que puede mostrar la lista de farmacias. La pantalla solo
 * "dibuja" el estado que le llega; no toma decisiones.
 */
sealed class FarmaciaUiState {

    /** Primera carga y todavía no hay nada guardado en Room: ruedita. */
    object Cargando : FarmaciaUiState()

    /**
     * Hay farmacias para mostrar (puede estar vacía si el buscador no
     * encontró nada). [refrescando] = se está consultando la API.
     */
    data class Exito(
        val farmacias: List<FarmaciaUi>,
        val refrescando: Boolean = false
    ) : FarmaciaUiState()

    /**
     * No se pudo hablar con el servidor, pero hay datos guardados en Room:
     * se muestran con el aviso "Sin conexión, mostrando datos guardados".
     */
    data class SinConexion(
        val farmacias: List<FarmaciaUi>,
        val mensaje: String
    ) : FarmaciaUiState()

    /** Falló la red y no hay nada guardado: no hay qué mostrar. */
    data class Error(val mensaje: String) : FarmaciaUiState()
}

/**
 * El ViewModel sobrevive a la rotación de pantalla y arma el estado de la UI
 * combinando:
 *  - las farmacias que hay en Room (filtradas por el buscador local), y
 *  - cómo salió el último intento de hablar con la API.
 */
class FarmaciaViewModel(
    private val repository: FarmaciaRepository,
    private val busquedaRepository: BusquedaRepository = BusquedaRepository()
) : ViewModel() {

    // ── Búsqueda online (la que ya tenía la app, no cambia) ─────────────────
    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    fun limpiarBusqueda() {
        _searchState.value = SearchUiState.Idle
    }

    fun buscar(query: String) {
        if (query.isBlank()) {
            _searchState.value = SearchUiState.Idle
            return
        }
        viewModelScope.launch {
            _searchState.value = SearchUiState.Cargando
            try {
                val results = busquedaRepository.buscar(query)
                _searchState.value = SearchUiState.Exito(results)
            } catch (e: Exception) {
                _searchState.value = SearchUiState.Error(e.message ?: "Error al buscar")
            }
        }
    }

    // ── Buscador local con Room ──────────────────────────────────────────────
    // Texto que el usuario escribe en el campo "Filtrar farmacias".
    private val _filtroLocal = MutableStateFlow("")
    val filtroLocal: StateFlow<String> = _filtroLocal.asStateFlow()

    /** Lo llama el campo de búsqueda cada vez que cambia el texto. */
    fun filtrarLocal(texto: String) {
        _filtroLocal.value = texto
    }

    // Cada vez que cambia el texto, flatMapLatest CANCELA la consulta anterior
    // y se suscribe a la nueva consulta del DAO. Así la lista se actualiza en
    // tiempo real mientras el usuario escribe, y el filtrado lo hace SQLite
    // (con LIKE), no un .filter { } sobre una lista en memoria.
    // Emitimos el par (texto, lista) para saber a qué búsqueda pertenece cada lista.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val farmaciasFiltradas = _filtroLocal.flatMapLatest { texto ->
        val consulta = if (texto.isBlank()) {
            repository.farmacias            // sin filtro: todas
        } else {
            repository.buscar(texto)        // SELECT ... WHERE nombre LIKE ...
        }
        consulta.map { lista -> texto to lista }
    }

    // ── Estado de la lista de farmacias ──────────────────────────────────────
    // Estado "interno" (privado y mutable)...
    private val refrescando = MutableStateFlow(false)
    private val ultimoResultado = MutableStateFlow<ResultadoRefresco?>(null)

    // ...y estado "público" (solo lectura) que observa la pantalla.
    val uiState: StateFlow<FarmaciaUiState> = combine(
        farmaciasFiltradas,
        refrescando,
        ultimoResultado
    ) { (texto, farmacias), cargando, resultado ->
        armarEstado(texto, farmacias, cargando, resultado)
    }.stateIn(
        scope = viewModelScope,
        // Sigue escuchando 5 s después de que la pantalla deja de observar
        // (alcanza para una rotación sin reiniciar todo).
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FarmaciaUiState.Cargando
    )

    // ── Selección para las pantallas de detalle (no cambia) ──────────────────
    private val _selectedFarmacia = MutableStateFlow<FarmaciaUi?>(null)
    val selectedFarmacia: StateFlow<FarmaciaUi?> = _selectedFarmacia.asStateFlow()

    fun seleccionar(farmacia: FarmaciaUi) {
        _selectedFarmacia.value = farmacia
    }

    private val _selectedResult = MutableStateFlow<SearchResult?>(null)
    val selectedResult: StateFlow<SearchResult?> = _selectedResult.asStateFlow()

    fun seleccionarResult(result: SearchResult) {
        _selectedResult.value = result
    }

    init {
        // Al abrir: si Room está vacío va a la API; si no, no hace nada
        // y se muestra al instante lo guardado.
        refrescar(forzar = false)
    }

    /** Lo llaman el botón "Actualizar" y el botón "Reintentar". */
    fun refrescar(forzar: Boolean = true) {
        if (refrescando.value) return              // evita pedidos dobles
        viewModelScope.launch {
            refrescando.value = true
            ultimoResultado.value = repository.refrescar(forzar)
            refrescando.value = false
        }
    }

    /** Traduce "datos + resultado de red" a UN estado de pantalla. */
    private fun armarEstado(
        texto: String,
        farmacias: List<FarmaciaUi>,
        cargando: Boolean,
        resultado: ResultadoRefresco?
    ): FarmaciaUiState {
        val mensaje = mensajeDeError(resultado)
        // Si el usuario está filtrando, una lista vacía significa
        // "no hubo coincidencias", no "la base está vacía".
        val hayQueMostrarLista = farmacias.isNotEmpty() || texto.isNotBlank()

        return when {
            // Hay datos guardados pero falló la red: los mostramos igual.
            hayQueMostrarLista && mensaje != null && !cargando ->
                FarmaciaUiState.SinConexion(farmacias, mensaje)

            hayQueMostrarLista ->
                FarmaciaUiState.Exito(farmacias, refrescando = cargando)

            // De acá para abajo: Room está vacío.
            cargando || resultado == null -> FarmaciaUiState.Cargando

            mensaje != null -> FarmaciaUiState.Error(mensaje)

            // La API respondió bien pero no hay farmacias de turno.
            else -> FarmaciaUiState.Exito(emptyList())
        }
    }

    /** null = no hubo error. */
    private fun mensajeDeError(resultado: ResultadoRefresco?): String? =
        when (resultado) {
            is ResultadoRefresco.SinConexion ->
                "Sin conexión, mostrando datos guardados"
            is ResultadoRefresco.ErrorServidor ->
                "El servidor respondió con error ${resultado.codigo}"
            is ResultadoRefresco.ErrorDesconocido ->
                "No se pudieron actualizar las farmacias"
            is ResultadoRefresco.Exito,
            ResultadoRefresco.SinCambios,
            null -> null
        }

    companion object {
        /**
         * Fábrica manual (sin Hilt): le dice a Android CÓMO crear este
         * ViewModel, que necesita un repositorio en el constructor.
         * Uso en Compose:
         *   val vm: FarmaciaViewModel = viewModel(
         *       factory = FarmaciaViewModel.factory(applicationContext)
         *   )
         */
        fun factory(context: Context): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val db = BuscaYaDatabase.getInstance(context)
                    FarmaciaViewModel(FarmaciaRepository(db, RetrofitClient.api))
                }
            }
    }
}
