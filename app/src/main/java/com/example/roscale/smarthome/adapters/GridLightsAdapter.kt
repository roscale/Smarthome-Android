package com.example.roscale.smarthome.adapters

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView
import com.example.roscale.smarthome.R
import com.example.roscale.smarthome.doAsync
import com.example.roscale.smarthome.sendTCPMessage
import kotlinx.android.synthetic.main.item_grid_lights.view.*
import org.greenrobot.eventbus.EventBus


data class GridLightsItemData(var name: String, val UUID: String, val ip: String, val powerState: Boolean)

class LightOn
class AllLightsOff

class GridLightsViewHolder(val grid_lights: RecyclerView, itemView: View): RecyclerView.ViewHolder(itemView) {
    fun areAllLightsOff(): Boolean {
        for (i in 0 until grid_lights.adapter!!.itemCount) {
            val holder = grid_lights.findViewHolderForLayoutPosition(i)
            if (holder?.itemView?.findViewById<Switch>(R.id.toggle_light)?.isChecked!!) {
                return false
            }
        }
        return true
    }

    fun bind(itemData: GridLightsItemData) {
        itemView.light_name.text = itemData.name

        itemView.toggle_light.setOnCheckedChangeListener { compoundButton, b ->
            if (itemView.toggle_light.isChecked) {
                doAsync { sendTCPMessage(itemData.ip, itemView.context.resources.getInteger(R.integer.command_port), "on") }.execute()

                println("ON ${itemData.name} ${itemData.UUID} ${itemData.ip}")
                EventBus.getDefault().post(LightOn())

            } else {
                doAsync { sendTCPMessage(itemData.ip, itemView.context.resources.getInteger(R.integer.command_port), "off") }.execute()

                println("OFF ${itemData.name} ${itemData.UUID} ${itemData.ip}")
                if (areAllLightsOff()) {
                    EventBus.getDefault().post(AllLightsOff())
                }
            }
        }
    }
}

class GridLightsAdapter(val data: List<GridLightsItemData>) : RecyclerView.Adapter<GridLightsViewHolder>() {
    lateinit var rv: RecyclerView
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        rv = recyclerView
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridLightsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_grid_lights, parent, false)
        return GridLightsViewHolder(rv, v)
    }

    override fun onBindViewHolder(holder: GridLightsViewHolder, position: Int) {
        val itemData = data[position]
        holder.bind(itemData)
    }
}

class LinearEqualSpaceItemDecoration(val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.top = space
        outRect.left = space
        outRect.right = space

        if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount-1) {
            outRect.bottom = space
        }
    }
}