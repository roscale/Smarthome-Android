package com.example.roscale.smarthome.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.roscale.smarthome.database.AppDatabase
import com.example.roscale.smarthome.database.Light

// ViewModel containing a LiveData listening to changes in the Lights table
class LightsModel : ViewModel() {
    private var init = false
    private lateinit var lights_list: LiveData<List<Light>>

    fun getLights(applicationContext: Context): LiveData<List<Light>> {
        if (!init) {
            init = true
            loadLights(applicationContext)
        }
        return lights_list
    }

    private fun loadLights(applicationContext: Context) {
        lights_list = AppDatabase.instance(applicationContext).lightDao().findAll()
    }

}