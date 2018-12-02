package com.example.roscale.smarthome.adapters

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.roscale.smarthome.*
import com.example.roscale.smarthome.database.AppDatabase
import com.example.roscale.smarthome.widgets.TogglerWidget
import kotlinx.android.synthetic.main.dialog_rename.*
import kotlinx.android.synthetic.main.item_lights.view.*

data class LightsItemData(var name: String, val UUID: String, val ip: String, val powerState: Boolean)


class LightOn
class AllLightsOff


class LightsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    // Fix the switch animation by not toggling it twice (first time manually, second time by a change in the database)
    var manuallyPowered = false

    fun bind(itemData: LightsItemData) {
        itemView.light_name.text = itemData.name
        itemView.toggle_light.isChecked = itemData.powerState

        // Create the context menu
        itemView.setOnCreateContextMenuListener { contextMenu, view, contextMenuInfo ->

            contextMenu.add("Rename").setOnMenuItemClickListener {
                AlertDialog.Builder(itemView.context)
                    .setView(R.layout.dialog_rename)
                    .setTitle("Rename")

                    // Update the database and the widgets
                    .setPositiveButton("Rename") { dialog, which ->
                        val text = (dialog as AlertDialog).nameEditText.text
                        if (text.isNotBlank()) {
                            doAsync { AppDatabase.instance(view.context).lightDao().rename(text.toString(), itemData.UUID) }.execute()
                            updateWidgets(itemView.context, itemData.UUID, text.toString())
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        print("CANCELED")

                    // Show the dialog and pop up the keyboard automatically
                    }.create().apply {
                        show()
                        nameEditText.requestFocus()
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                    }

                true
            }

            // Update the database and invalidate the widgets
            contextMenu.add("Delete").setOnMenuItemClickListener {
                doAsync { AppDatabase.instance(itemView.context).lightDao().deleteByUUID(itemData.UUID) }.execute()
                invalidateWidgets(itemView.context, itemData.UUID)
                true
            }
        }

        // Listen for checked state change (manually or by a change in database)
        itemView.toggle_light.setOnCheckedChangeListener { compoundButton, b ->
            doAsync { sendTCPMessage(itemData.ip, itemView.context.resources.getInteger(R.integer.command_port), if (itemView.toggle_light.isChecked) "on" else "off") }.execute()
            updateWidgets(itemView.context, itemData.UUID, itemData.name, itemView.toggle_light.isChecked)
        }

        // Listen for click (only manually)
        itemView.toggle_light.setOnClickListener {
            manuallyPowered = true
            doAsync { AppDatabase.instance(itemView.context).lightDao().turn(itemView.toggle_light.isChecked, itemData.UUID) }.execute()
        }
    }
}


class LightsAdapter(var data: List<LightsItemData>) : RecyclerView.Adapter<LightsViewHolder>() {
    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_lights, parent, false)
        return LightsViewHolder(v)
    }

    override fun onBindViewHolder(holder: LightsViewHolder, position: Int) {
        val itemData = data[position]
        holder.bind(itemData)
    }

    // Receive the payloads the fragment and update the ViewHolder
    override fun onBindViewHolder(holder: LightsViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val o = payloads[0] as Bundle
            if (o.containsKey("powerState")) {

                // Toggle the switch if not done manually
                if (!holder.manuallyPowered) {
                    holder.itemView.toggle_light.isChecked = o.getBoolean("powerState")
                }
                holder.manuallyPowered = false
            }
        }
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