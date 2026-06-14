package com.example.myceti.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myceti.data.model.Comunicado
import com.example.myceti.databinding.ItemComunicadoBinding

class ComunicadosAdapter : ListAdapter<Comunicado, ComunicadosAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemComunicadoBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(c: Comunicado) {
            b.tvTitulo.text = c.titulo
            b.tvDescripcion.text = c.descripcion
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemComunicadoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Comunicado>() {
            override fun areItemsTheSame(a: Comunicado, b: Comunicado) = a.id == b.id
            override fun areContentsTheSame(a: Comunicado, b: Comunicado) = a == b
        }
    }
}