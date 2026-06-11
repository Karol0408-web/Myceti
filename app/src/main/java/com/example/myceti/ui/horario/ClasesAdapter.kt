package com.example.myceti.ui.horario

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myceti.data.model.Clase
import com.example.myceti.databinding.ItemClaseBinding

class ClasesAdapter : ListAdapter<Clase, ClasesAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemClaseBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(c: Clase) {
            b.tvHoraInicio.text = c.horaInicio
            b.tvHoraFin.text    = c.horaFin
            b.tvMateria.text    = c.materia
            b.tvSalon.text      = "Salón ${c.salon} · Edificio ${c.edificio}"
            b.tvMaestro.text    = "MAESTRO: ${c.maestro}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemClaseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Clase>() {
            override fun areItemsTheSame(a: Clase, b: Clase) = a.id == b.id
            override fun areContentsTheSame(a: Clase, b: Clase) = a == b
        }
    }
}