package com.example.roscale.smarthome.fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.example.roscale.smarthome.R
import com.example.roscale.smarthome.activities.AddLightsActivity
import com.example.roscale.smarthome.adapters.*
import com.example.roscale.smarthome.viewModels.LightsModel
import kotlinx.android.synthetic.main.fragment_lights.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class LightsFragment : Fragment() {


//    class LightsViewModel : ViewModel() {
//        val powerStates = MutableLiveData<ArrayList<Int>>()
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        val mm = ViewModelProviders.of(this).get(LightsViewModel::class.java)
//        mm.powerStates.observe(this, Observer {
//            println("CHANGE DETECTED")
//        })
//
//        mm.powerStates.value = ArrayList()
//        mm.powerStates.postValue(mm.powerStates.value)

        val model = ViewModelProviders.of(this).get(LightsModel::class.java)
        model.getLights(activity!!.applicationContext).observe(this, Observer { it ->
            val data = ArrayList<GridLightsItemData>()
            it.forEach { data.add(GridLightsItemData(it.name!!, it.uuid!!, it.ip!!, it.power_state!!)) }
            grid_lights.adapter = GridLightsAdapter(data)
            grid_lights.adapter!!.notifyDataSetChanged()
        })

        grid_lights.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )
        grid_lights.addItemDecoration(LinearEqualSpaceItemDecoration(20))

        toggle_all_lights.setOnClickListener {
            for (i in 0 until grid_lights.adapter!!.itemCount) {
                val holder = grid_lights.findViewHolderForLayoutPosition(i)
                holder?.itemView?.findViewById<Switch>(R.id.toggle_light)?.isChecked = toggle_all_lights.isChecked
            }
        }

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
        super.onStop()
        EventBus.getDefault().unregister(this)
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
