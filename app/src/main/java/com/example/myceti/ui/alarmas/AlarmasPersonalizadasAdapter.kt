package com.example.myceti.ui.alarmas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myceti.data.model.AlarmaPersonalizada
import com.example.myceti.databinding.ItemAlarmaPersonalizadaBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AlarmasPersonalizadasAdapter(
    private val onEliminar: (AlarmaPersonalizada) -> Unit
) : ListAdapter<AlarmaPersonalizada, AlarmasPersonalizadasAdapter.VH>(DIFF) {

    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class VH(private val b: ItemAlarmaPersonalizadaBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(a: AlarmaPersonalizada) {
            b.tvTituloAlarma.text = a.titulo
            b.tvFechaAlarma.text = a.fecha?.toDate()?.let { sdf.format(it) } ?: "--"
            b.btnEliminarAlarma.setOnClickListener { onEliminar(a) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemAlarmaPersonalizadaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AlarmaPersonalizada>() {
            override fun areItemsTheSame(a: AlarmaPersonalizada, b: AlarmaPersonalizada) = a.id == b.id
            override fun areContentsTheSame(a: AlarmaPersonalizada, b: AlarmaPersonalizada) = a == b
        }
    }
}