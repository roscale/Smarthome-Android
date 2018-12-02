package com.example.roscale.smarthome.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roscale.smarthome.R
import com.example.roscale.smarthome.activities.AddLightsActivity
import com.example.roscale.smarthome.adapters.*
import com.example.roscale.smarthome.database.AppDatabase
import com.example.roscale.smarthome.doAsync
import com.example.roscale.smarthome.viewModels.LightsModel
import kotlinx.android.synthetic.main.fragment_lights.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class LightsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toggle all the lights
        toggle_all_lights.setOnClickListener {
            doAsync { AppDatabase.instance(context!!).lightDao().turnAll(toggle_all_lights.isChecked) }.execute()
        }

        // Setup RecyclerView
        grid_lights.adapter = LightsAdapter(arrayListOf())
        grid_lights.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )
        grid_lights.addItemDecoration(LinearEqualSpaceItemDecoration(20))

        // Detect database changes through the Lights model
        val lightsModel = ViewModelProviders.of(activity!!).get(LightsModel::class.java)
        lightsModel.getLights(activity!!).observe(this, Observer { it ->

            // Globally notify the change
            if (it.any { it.power_state == true }) {
                EventBus.getDefault().post(LightOn())
            }
            if (it.all { it.power_state == false }) {
                EventBus.getDefault().post(AllLightsOff())
            }

            // Calculate the difference between the old/new data
            val oldData = (grid_lights.adapter as LightsAdapter).data
            val newData = it.map { LightsItemData(it.name!!, it.uuid!!, it.ip!!, it.power_state!!) }

            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldData.size
                override fun getNewListSize(): Int = newData.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldData[oldItemPosition].UUID == newData[newItemPosition].UUID
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldData[oldItemPosition] == newData[newItemPosition]
                }

                override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                    val old = oldData[oldItemPosition]
                    val new = newData[newItemPosition]

                    val diff = Bundle()

                    // Handle the power state change separately
                    // Instead of replacing the item, create a payload with the specific change
                    if (old.powerState != new.powerState) {
                        diff.putBoolean("powerState", new.powerState)
                    }
                    return if (diff.size() == 0) {
                        null
                    } else diff
                }
            })

            // Notify the adapter about the difference (including payloads)
            (grid_lights.adapter!! as LightsAdapter).data = newData
            result.dispatchUpdatesTo(grid_lights.adapter!!)
        })

        // Handle the adding lights button
        add_lights_button.setOnClickListener {
            val intent = Intent(context, AddLightsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToggledLight(event: LightOn) {
        toggle_all_lights.isChecked = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToggledLight(event: AllLightsOff) {
        toggle_all_lights.isChecked = false
    }
}
