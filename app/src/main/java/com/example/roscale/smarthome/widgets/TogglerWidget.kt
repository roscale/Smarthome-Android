package com.example.roscale.smarthome.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.roscale.smarthome.R
import android.app.PendingIntent
import com.example.roscale.smarthome.database.AppDatabase
import com.example.roscale.smarthome.doAsync
import com.example.roscale.smarthome.sendTCPMessage


class TogglerWidget : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        when {
            // Update on rename or power state change (by a change in database)
            intent?.action == ACTION_UPDATE_WIDGETS -> {
                val UUID = intent.getStringExtra(EXTRA_UUID)

                val powerStateChanged = intent.hasExtra(EXTRA_POWER_STATE)
                val nameChanged = intent.hasExtra(EXTRA_NAME)

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(ComponentName(context!!, this::class.java))

                for (id in ids) {
                    if (TogglerWidgetConfigureActivity.loadStringPref(context, id, EXTRA_UUID) == UUID) {
                        if (powerStateChanged) {
                            val powerState = intent.getBooleanExtra(EXTRA_POWER_STATE, false).toString()
                            TogglerWidgetConfigureActivity.saveStringPref(context, id, EXTRA_POWER_STATE, powerState)
                        }
                        if (nameChanged) {
                            val name = intent.getStringExtra(EXTRA_NAME)
                            TogglerWidgetConfigureActivity.saveStringPref(context, id, EXTRA_NAME, name)
                        }

                        updateAppWidget(context, appWidgetManager, id)
                    }
                }
            }

            // Invalidate deleted widgets
            intent?.action == ACTION_INVALIDATE_WIDGETS -> {
                val UUID = intent.getStringExtra(EXTRA_UUID)

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(ComponentName(context!!, this::class.java))

                for (id in ids) {
                    if (TogglerWidgetConfigureActivity.loadStringPref(context, id, EXTRA_UUID) == UUID) {
                        TogglerWidgetConfigureActivity.saveStringPref(context, id, EXTRA_UUID, "invalid")
                        TogglerWidgetConfigureActivity.saveStringPref(context, id, EXTRA_NAME, "Light deleted")
                        TogglerWidgetConfigureActivity.saveStringPref(context, id, EXTRA_IP, "127.0.0.1")
                        TogglerWidgetConfigureActivity.saveStringPref(context, id, EXTRA_POWER_STATE, false.toString())

                        updateAppWidget(context, appWidgetManager, id)
                    }
                }
            }

            // Send TCP message && update database && update concerning widgets
            intent?.action == ACTION_TOGGLE_LIGHT -> {
                val UUID = intent.getStringExtra(EXTRA_UUID)
                val ip = intent.getStringExtra(EXTRA_IP)
                val powerState = intent.getStringExtra(EXTRA_POWER_STATE).toBoolean()

                doAsync {
                    sendTCPMessage(ip, context!!.resources.getInteger(R.integer.command_port), if (powerState) "off" else "on")
                    AppDatabase.instance(context).lightDao().turn(!powerState, UUID)
                }.execute()

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(ComponentName(context!!, this::class.java))

                for (id in ids) {
                    if (TogglerWidgetConfigureActivity.loadStringPref(context, id, EXTRA_UUID) == UUID) {
                        TogglerWidgetConfigureActivity.saveStringPref(context, id, EXTRA_POWER_STATE, (!powerState).toString())

                        updateAppWidget(context, appWidgetManager, id)
                    }
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            TogglerWidgetConfigureActivity.deleteStringPref(context, appWidgetId, EXTRA_NAME)
            TogglerWidgetConfigureActivity.deleteStringPref(context, appWidgetId, EXTRA_UUID)
            TogglerWidgetConfigureActivity.deleteStringPref(context, appWidgetId, EXTRA_IP)
            TogglerWidgetConfigureActivity.deleteStringPref(context, appWidgetId, EXTRA_POWER_STATE)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        const val ACTION_TOGGLE_LIGHT = "Smarthome.TogglerWidget.ACTION_TOGGLE_LIGHT"
        const val ACTION_UPDATE_WIDGETS = "Smarthome.TogglerWidget.UPDATE_WIDGETS"
        const val ACTION_INVALIDATE_WIDGETS = "Smarthome.TogglerWidget.INVALIDATE_WIDGETS"

        const val EXTRA_UUID = "uuid"
        const val EXTRA_NAME = "name"
        const val EXTRA_IP = "ip"
        const val EXTRA_POWER_STATE = "power_state"

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val UUID = TogglerWidgetConfigureActivity.loadStringPref(context, appWidgetId, EXTRA_UUID)
            val name = TogglerWidgetConfigureActivity.loadStringPref(context, appWidgetId, EXTRA_NAME)
            val ip = TogglerWidgetConfigureActivity.loadStringPref(context, appWidgetId, EXTRA_IP)
            val powerState = TogglerWidgetConfigureActivity.loadStringPref(context, appWidgetId, EXTRA_POWER_STATE)

            val views = RemoteViews(context.packageName, R.layout.toggler_widget)

            // Update the layout of deleted widgets
            if (UUID == "invalid") {
                views.setImageViewResource(R.id.image, R.drawable.ic_invalid_light)
                views.setTextViewText(R.id.name, "Deleted")
                views.setOnClickPendingIntent(R.id.widget, null)

            } else {
                views.setTextViewText(R.id.name, name)
                views.setImageViewResource(R.id.image, if (powerState.toBoolean()) R.drawable.ic_lightbulb_on else R.drawable.ic_lightbulb_off)

                // Prepare onClick intent
                val intent = Intent(context, TogglerWidget::class.java)
                intent.action = ACTION_TOGGLE_LIGHT
                intent.putExtra(EXTRA_UUID, UUID)
                intent.putExtra(EXTRA_IP, ip)
                intent.putExtra(EXTRA_POWER_STATE, powerState)

                val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                views.setOnClickPendingIntent(R.id.widget, pendingIntent)
            }

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

