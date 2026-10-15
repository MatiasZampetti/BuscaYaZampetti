package com.example.buscayazampetti.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.buscayazampetti.data.model.FarmaciaUi
import com.example.buscayazampetti.databinding.ItemFarmaciaBinding

// Mismos colores de avatar que usa la pantalla en Compose.
private val coloresAvatar = listOf(
    "#00C48C", "#2B6FFF", "#FF6B6B", "#FF9F43", "#9B59B6"
).map { Color.parseColor(it) }

/**
 * Adapter del RecyclerView de farmacias.
 *
 * ListAdapter + DiffUtil: cuando Room emite una lista nueva (por ejemplo,
 * cada vez que el usuario escribe en el buscador), le pasamos la lista con
 * submitList() y DiffUtil calcula SOLO las filas que cambiaron, en vez de
 * redibujar todo con notifyDataSetChanged().
 */
class FarmaciaAdapter(
    private val onVerDetalle: (FarmaciaUi) -> Unit
) : ListAdapter<FarmaciaUi, FarmaciaAdapter.FarmaciaViewHolder>(DIFF) {

    /** El ViewHolder guarda las vistas de UNA fila para reutilizarlas al scrollear. */
    class FarmaciaViewHolder(val binding: ItemFarmaciaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            // Avatar redondo
            binding.flAvatar.clipToOutline = true
        }
    }

    // Se llama pocas veces: crea (infla) una fila nueva.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmaciaViewHolder {
        val binding = ItemFarmaciaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FarmaciaViewHolder(binding)
    }

    // Se llama muchas veces: carga los datos de una farmacia en una fila ya creada.
    override fun onBindViewHolder(holder: FarmaciaViewHolder, position: Int) {
        val farmacia = getItem(position)
        val b = holder.binding

        b.flAvatar.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(coloresAvatar.getOrElse(farmacia.colorIndex) { coloresAvatar[1] })
        }
        b.tvIniciales.text = farmacia.iniciales
        // Si la imagen no carga (por ejemplo, sin conexión) queda vacía
        // y se ven las iniciales que están debajo.
        b.ivImagen.load(farmacia.imagen.ifBlank { null }) { crossfade(true) }

        if (farmacia.esHoy) {
            b.tvDiaSemana.text = "Hoy"
            b.tvDiaSemana.setTextColor(Color.parseColor("#00A376"))
            b.tvDiaSemana.setTypeface(null, android.graphics.Typeface.BOLD)
            b.tvDiaSemana.background = GradientDrawable().apply {
                cornerRadius = 6 * b.root.resources.displayMetrics.density
                setColor(Color.parseColor("#1F00C48C"))
            }
        } else {
            b.tvDiaSemana.text = farmacia.diaSemana
            b.tvDiaSemana.setTextColor(Color.parseColor("#8A94A6"))
            b.tvDiaSemana.setTypeface(null, android.graphics.Typeface.NORMAL)
            b.tvDiaSemana.background = null
        }
        b.tvDia.text = farmacia.dia.toString()
        b.tvMes.text = farmacia.mes
        b.tvNombre.text = farmacia.nombre
        b.tvDireccion.text = farmacia.direccion

        b.btnMasInfo.setOnClickListener { onVerDetalle(farmacia) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FarmaciaUi>() {
            // ¿Es la misma farmacia? (mismo turno: nombre + fecha)
            override fun areItemsTheSame(a: FarmaciaUi, b: FarmaciaUi) =
                a.nombre == b.nombre && a.dia == b.dia && a.mes == b.mes

            // ¿Cambió algo de lo que se muestra?
            override fun areContentsTheSame(a: FarmaciaUi, b: FarmaciaUi) = a == b
        }
    }
}
