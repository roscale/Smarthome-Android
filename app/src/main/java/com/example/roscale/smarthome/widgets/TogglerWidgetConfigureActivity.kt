package com.example.roscale.smarthome.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roscale.smarthome.R
import com.example.roscale.smarthome.adapters.LinearEqualSpaceItemDecoration
import com.example.roscale.smarthome.database.AppDatabase
import com.example.roscale.smarthome.database.Light
import kotlinx.android.synthetic.main.item_widget_add_light.view.*
import kotlinx.android.synthetic.main.toggler_widget_configure.*

data class WidgetLightData(val name: String, val UUID: String, val ip: String, val powerState: Boolean)

/**
 * The configuration screen for the [TogglerWidget] AppWidget.
 */
class TogglerWidgetConfigureActivity : AppCompatActivity() {
    internal var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID


    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var data: WidgetLightData

        fun bind(data: WidgetLightData) {
            this.data = data
            itemView.light_name.text = data.name

            itemView.card.setOnClickListener {
                val context = this@TogglerWidgetConfigureActivity

                // Save the widget data
                TogglerWidgetConfigureActivity.saveStringPref(context, mAppWidgetId, TogglerWidget.EXTRA_NAME, this.data.name)
                TogglerWidgetConfigureActivity.saveStringPref(context, mAppWidgetId, TogglerWidget.EXTRA_UUID, this.data.UUID)
                TogglerWidgetConfigureActivity.saveStringPref(context, mAppWidgetId, TogglerWidget.EXTRA_IP, this.data.ip)
                TogglerWidgetConfigureActivity.saveStringPref(context, mAppWidgetId, TogglerWidget.EXTRA_POWER_STATE, this.data.powerState.toString())

                // It is the responsibility of the configuration activity to update the app widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                TogglerWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId)

                // Create the widget
                // Make sure we pass back the original appWidgetId
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
                finish()
            }
        }
    }


    inner class Adapter(val array: List<WidgetLightData>) : RecyclerView.Adapter<Holder>() {
        override fun getItemCount(): Int = array.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_widget_add_light, parent, false)
            return Holder(v)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val itemData = array[position]
            holder.bind(itemData)
        }
    }


    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.toggler_widget_configure)

        rv.layoutManager = LinearLayoutManager(
            applicationContext,
            RecyclerView.VERTICAL,
            false
        )
        rv.addItemDecoration(LinearEqualSpaceItemDecoration(20))

        // Populate the RecyclerView
        AppDatabase.instance(applicationContext).lightDao().findAll().observe(this, Observer<List<Light>> { lightList ->
            val data = lightList.map { WidgetLightData(it.name!!, it.uuid!!, it.ip!!, it.power_state!!) }
            rv.adapter = Adapter(data)
        })

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    companion object {

        private const val PREFS_WIDGET_NAME = "com.example.roscale.smarthome.widgets.TogglerWidget"
        private const val PREF_PREFIX_KEY = "appwidget_"

        // Write the prefix to the SharedPreferences object for this widget
        internal fun saveStringPref(context: Context, appWidgetId: Int, key: String, value: String) {
            val prefs = context.getSharedPreferences(PREFS_WIDGET_NAME, 0).edit()
            prefs.putString("$PREF_PREFIX_KEY${appWidgetId}_$key", value)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        internal fun loadStringPref(context: Context, appWidgetId: Int, key: String): String {
            val prefs = context.getSharedPreferences(PREFS_WIDGET_NAME, 0)
            val titleValue = prefs.getString("$PREF_PREFIX_KEY${appWidgetId}_$key", null)
            return titleValue ?: "error"
        }

        internal fun deleteStringPref(context: Context, appWidgetId: Int, key: String) {
            val prefs = context.getSharedPreferences(PREFS_WIDGET_NAME, 0).edit()
            prefs.remove("$PREF_PREFIX_KEY${appWidgetId}_$key")
            prefs.apply()
        }
    }
}

