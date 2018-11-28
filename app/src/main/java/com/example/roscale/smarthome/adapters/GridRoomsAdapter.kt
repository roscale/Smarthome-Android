package com.example.roscale.smarthome.adapters

import android.graphics.Rect
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import com.example.roscale.smarthome.R
import kotlinx.android.synthetic.main.item_grid_rooms.view.*
import org.greenrobot.eventbus.EventBus

data class GridRoomsItemData(var name: String)

class GroupOn
class AllGroupsOff

class GridRoomsViewHolder(val grid_rooms: RecyclerView, itemView: View): RecyclerView.ViewHolder(itemView) {
    fun areAllGroupsOff(): Boolean {
        for (i in 0 until grid_rooms.adapter!!.itemCount) {
            val holder = grid_rooms.findViewHolderForLayoutPosition(i)
            if (holder?.itemView?.findViewById<Switch>(R.id.toggle_group)?.isChecked!!) {
                return false
            }
        }
        return true
    }

    fun bind(itemData: GridRoomsItemData) {
        itemView.group_name.text = itemData.name

        itemView.toggle_group.setOnCheckedChangeListener { switch: CompoundButton, checked: Boolean ->
            if (checked) {
                println("ON ${itemView.group_name.text}")
                EventBus.getDefault().post(GroupOn())
            } else {
                println("OFF ${itemView.group_name.text}")
                if (areAllGroupsOff()) {
                    EventBus.getDefault().post(AllGroupsOff())
                }
            }
        }
    }
}

class GridRoomsAdapter(val data: List<GridRoomsItemData>) : RecyclerView.Adapter<GridRoomsViewHolder>() {
    lateinit var rv: RecyclerView
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        rv = recyclerView
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridRoomsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_grid_rooms, parent, false)
        return GridRoomsViewHolder(rv, v)
    }

    override fun onBindViewHolder(holder: GridRoomsViewHolder, position: Int) {
        val itemData = data[position]
        holder.bind(itemData)
    }
}

class GridEqualSpaceItemDecoration(val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = space
        outRect.top = space

        val spanCount = (parent.layoutManager as GridLayoutManager).spanCount
        val viewPos = parent.getChildAdapterPosition(view)

        if ((viewPos+1) % spanCount == 0) {
            outRect.right = space
        }

        val lastItems = (state.itemCount % spanCount)
        if ((lastItems == 0 && (state.itemCount-1 - viewPos) < spanCount) ||
                lastItems != 0 && (state.itemCount-1 - viewPos) < lastItems) {
            outRect.bottom = space
        }
    }
}
