package com.example.myceti.ui.apuntes

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myceti.R
import com.example.myceti.data.model.Apunte
import com.example.myceti.databinding.ItemApunteBinding
import com.example.myceti.databinding.ItemHeaderMateriaBinding

sealed class ApunteItem {
    data class Header(val materia: String, val cantidad: Int) : ApunteItem()
    data class ApunteEntry(val apunte: Apunte) : ApunteItem()
}

class ApuntesAdapter(
    private val onEliminar: (Apunte) -> Unit,
    private val onVerImagen: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ApunteItem> = emptyList()

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_APUNTE = 1
    }

    fun submitApuntes(apuntes: List<Apunte>, filtro: String = "Todos") {
        val filtrados = if (filtro == "Todos") apuntes else apuntes.filter { it.materia == filtro }
        val grouped = filtrados.groupBy { it.materia }
        val lista = mutableListOf<ApunteItem>()
        grouped.forEach { (materia, lista_apuntes) ->
            lista.add(ApunteItem.Header(materia, lista_apuntes.size))
            lista_apuntes.forEach { lista.add(ApunteItem.ApunteEntry(it)) }
        }
        items = lista
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ApunteItem.Header -> TYPE_HEADER
        is ApunteItem.ApunteEntry -> TYPE_APUNTE
    }

    override fun getItemCount() = items.size

    inner class HeaderVH(private val b: ItemHeaderMateriaBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(h: ApunteItem.Header) {
            b.tvNombreMateria.text = h.materia
            b.tvContador.text = "${h.cantidad} fotos"
        }
    }

    inner class ApunteVH(private val b: ItemApunteBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(entry: ApunteItem.ApunteEntry) {
            val a = entry.apunte
            if (!a.imageBase64.isNullOrEmpty()) {
                try {
                    val imageBytes = android.util.Base64.decode(a.imageBase64, android.util.Base64.DEFAULT)
                    Glide.with(b.root)
                        .load(imageBytes)
                        .centerCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(b.ivApunte)
                } catch (e: Exception) {
                    b.ivApunte.setImageResource(R.drawable.ic_person)
                }
            } else {
                b.ivApunte.setImageResource(R.drawable.ic_person)
            }

            // Ver imagen en grande
            b.ivApunte.setOnClickListener {
                if (!a.imageBase64.isNullOrEmpty()) {
                    onVerImagen(a.imageBase64)
                }
            }

            b.btnEliminar.setOnClickListener {
                AlertDialog.Builder(b.root.context)
                    .setTitle("Eliminar apunte")
                    .setMessage("¿Eliminar este apunte de ${a.materia}?")
                    .setPositiveButton("Eliminar") { _, _ -> onEliminar(a) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderVH(
                ItemHeaderMateriaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> ApunteVH(
                ItemApunteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ApunteItem.Header -> (holder as HeaderVH).bind(item)
            is ApunteItem.ApunteEntry -> (holder as ApunteVH).bind(item)
        }
    }
}