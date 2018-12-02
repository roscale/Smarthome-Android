package com.example.roscale.smarthome

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.roscale.smarthome.widgets.TogglerWidget
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket

val TAG = "Smarthome"

class doAsync(val handler: () -> Unit) : AsyncTask<Unit, Unit, Unit>() {
    var postHandler: () -> Unit = {}
    fun postExecute(postHandler: () -> Unit): doAsync {
        this.postHandler = postHandler
        return this
    }

    override fun doInBackground(vararg p0: Unit?) {
        handler()
    }

    override fun onPostExecute(result: Unit?) {
        postHandler()
    }
}

fun sendTCPMessage(ip: String, port: Int, message: String) {
    try {
        val s = Socket(ip, port)
        val out = BufferedWriter(OutputStreamWriter(s.getOutputStream()))

        out.write(message)

        out.flush()
        out.close()
        s.close()

    } catch (ex: Exception) {
        Log.e(TAG, "Couldn't send TCP message ${message} to ${ip}:${port}")
    }
}

fun broadcastUDPMessage(port: Int, message: String) {
    try {
        val s = DatagramSocket()
        val local = InetAddress.getByName("192.168.0.255")
        val msg_length = message.length
        val message = message.toByteArray()

        val p = DatagramPacket(message, msg_length, local, port)
        s.send(p)

    } catch (e: Exception) {
        Log.e(TAG, "Couldn't send UDP message ${message} on port ${port}")
    }
}

fun <VH: RecyclerView.ViewHolder> RecyclerView.forEachViewHolder(f: (holder: VH) -> Unit) {
    for (i in 0 until adapter!!.itemCount) {
        val holder = findViewHolderForAdapterPosition(i) as VH
        f(holder)
    }
}

fun updateWidgets(context: Context, UUID: String, name: String? = null, powerState: Boolean? = null) {
    val intent = Intent(context, TogglerWidget::class.java)
    intent.action = TogglerWidget.ACTION_UPDATE_WIDGETS
    intent.putExtra(TogglerWidget.EXTRA_UUID, UUID)

    if (name != null) {
        intent.putExtra(TogglerWidget.EXTRA_NAME, name)
    }
    if (powerState != null) {
        intent.putExtra(TogglerWidget.EXTRA_POWER_STATE, powerState)
    }
    context.sendBroadcast(intent)
}

fun invalidateWidgets(context: Context, UUID: String) {
    val intent = Intent(context, TogglerWidget::class.java)
    intent.action = TogglerWidget.ACTION_INVALIDATE_WIDGETS
    intent.putExtra(TogglerWidget.EXTRA_UUID, UUID)
    context.sendBroadcast(intent)
}