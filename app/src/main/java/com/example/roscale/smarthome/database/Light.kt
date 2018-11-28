package com.example.roscale.smarthome.database

import androidx.lifecycle.LiveData
import androidx.room.*


@Entity
data class Light(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "uuid") var uuid: String?,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "ip") var ip: String?,
    @ColumnInfo(name = "power_state") var power_state: Boolean?
)

@Dao
interface LightDao {
    @Query("SELECT * FROM Light")
    fun findAll(): LiveData<List<Light>>

    @Query("SELECT * FROM Light WHERE uuid = :uuid LIMIT 1")
    fun findByUUID(uuid: String): Light?

    @Insert
    fun insertAll(vararg lights: Light)

    @Delete
    fun delete(light: Light)
}