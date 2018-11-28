package com.example.roscale.smarthome.fragments


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roscale.smarthome.R
import com.example.roscale.smarthome.adapters.AddLightsAdapter
import com.example.roscale.smarthome.adapters.AddLightsItemData
import com.example.roscale.smarthome.adapters.AddLightsViewHolder
import com.example.roscale.smarthome.adapters.LinearEqualSpaceItemDecoration
import com.example.roscale.smarthome.database.AppDatabase
import com.example.roscale.smarthome.database.Light
import com.example.roscale.smarthome.doAsync
import com.example.roscale.smarthome.forEachViewHolder
import com.example.roscale.smarthome.broadcastUDPMessage
import kotlinx.android.synthetic.main.fragment_add_lights.*
import kotlinx.android.synthetic.main.item_add_lights.view.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket


data class Device(val ip: String, val UUID: String, val powerState: Boolean)

class DiscoverDevicesThread(val context: Context, val port: Int, val callback: (Device) -> Unit) : Thread() {
    lateinit var serverSocket: ServerSocket

    override fun run() {
        try {
            serverSocket = ServerSocket(port)
            serverSocket.soTimeout = 5000

            while (!interrupted()) {
                val socket = serverSocket.accept()
                val message = BufferedReader(InputStreamReader(socket.getInputStream())).readLine()

                val reader = JSONObject(message)
                Handler(context.mainLooper).post {
                    callback(Device(
                        socket.inetAddress.hostAddress,
                        reader.getString("uuid"),
                        reader.getInt("power_state") != 0))
                }
            }
        } catch (e: Exception) {
            return
        }
    }

    fun close() {
        serverSocket.close()
        interrupt()
    }
}


class AddLightsFragment : Fragment() {

    lateinit var data: ArrayList<AddLightsItemData>
    lateinit var discoverDevicesThread: DiscoverDevicesThread

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_lights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        data = arrayListOf()

        add_lights.adapter = AddLightsAdapter(data)
        add_lights.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )
        add_lights.addItemDecoration(LinearEqualSpaceItemDecoration(20))

        val ctx = context!!
        discoverDevicesThread = DiscoverDevicesThread(context!!, resources.getInteger(R.integer.discovery_port_listen)) {
            doAsync {
                if (AppDatabase.instance(ctx).lightDao().findByUUID(it.UUID) == null) {
                    Handler(ctx.mainLooper).post {
                        data.add(AddLightsItemData(it.ip, it.UUID, it.powerState))
                        add_lights.adapter!!.notifyDataSetChanged()
                    }
                }
            }.execute()
        }

        discoverDevicesThread.start()
        doAsync { broadcastUDPMessage(resources.getInteger(R.integer.discovery_port_send), "discovery") }.execute()

        Toast.makeText(context, "Searching lights...", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({
            try {
                discoverDevicesThread.close()
                searching.visibility = View.GONE
                next.visibility = View.VISIBLE
            } catch (e: java.lang.IllegalStateException) { }
        }, 5000)

        next.setOnClickListener {
            doAsync {
                add_lights.forEachViewHolder<AddLightsViewHolder> {
                    val itemview = it.itemView
                    if (itemview.check.isChecked) {
                        AppDatabase.instance(context!!.applicationContext).lightDao().insertAll(Light(
                            null,
                            it.data.UUID,
                            itemview.name.text.toString(),
                            it.data.ip,
                            it.data.powerState))
                    }
                }
            }.postExecute {
                activity?.finish()
            }.execute()
        }
    }

    override fun onStop() {
        discoverDevicesThread.close()
        super.onStop()
    }
}