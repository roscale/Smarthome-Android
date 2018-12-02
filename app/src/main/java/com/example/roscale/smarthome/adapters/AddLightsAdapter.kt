package com.example.roscale.smarthome.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.roscale.smarthome.R
import kotlinx.android.synthetic.main.item_add_lights.view.*

class LightNumberGenerator {
    companion object {
        private var i = 1
        fun next(): Int = i++
        fun reset() { i = 1 }
    }
}


data class AddLightsItemData(val ip: String, val UUID: String, val powerState: Boolean)


class AddLightsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    lateinit var data: AddLightsItemData

    init {
        itemView.nameEditText.setText("Light ${LightNumberGenerator.next()}", TextView.BufferType.EDITABLE)
    }

    fun bind(data: AddLightsItemData) {
        this.data = data
    }
}

class AddLightsAdapter(val array: List<AddLightsItemData>) : RecyclerView.Adapter<AddLightsViewHolder>() {
    init {
        LightNumberGenerator.reset()
    }

    override fun getItemCount(): Int = array.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddLightsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_add_lights, parent, false)
        return AddLightsViewHolder(v)
    }

    override fun onBindViewHolder(holder: AddLightsViewHolder, position: Int) {
        holder.bind(array[position])
    }
}