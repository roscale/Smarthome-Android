package com.example.roscale.smarthome.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.roscale.smarthome.R
import com.example.roscale.smarthome.adapters.*
import com.example.roscale.smarthome.forEachViewHolder
import kotlinx.android.synthetic.main.fragment_groups.*
import kotlinx.android.synthetic.main.item_grid_rooms.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class GroupsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = listOf(
            GridRoomsItemData("Bedroom"),
            GridRoomsItemData("Kitchen"),
            GridRoomsItemData("Living room")
        )

        grid_rooms.adapter = GridRoomsAdapter(data)
        grid_rooms.layoutManager = GridLayoutManager(this.context, 2)
        grid_rooms.addItemDecoration(GridEqualSpaceItemDecoration(30))

        toggle_all_groups.setOnClickListener {
            grid_rooms.forEachViewHolder<GridRoomsViewHolder> {
                it.itemView.toggle_group.isChecked = toggle_all_groups.isChecked
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToggledGroup(event: GroupOn) {
        toggle_all_groups.isChecked = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToggledGroup(event: AllGroupsOff) {
        toggle_all_groups.isChecked = false
    }
}
