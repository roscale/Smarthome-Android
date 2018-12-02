package com.example.roscale.smarthome.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Light::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lightDao(): LightDao

    companion object {
        private var initialized = false
        lateinit var db: AppDatabase

        fun instance(applicationContext: Context): AppDatabase {
            if (!initialized) {
                initialized = true
                db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "db"
                ).build()
            }
            return db
        }
    }
}