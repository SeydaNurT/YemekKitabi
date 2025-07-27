package com.example.yemektarifim.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.yemektarifim.model.Tarif

@Dao
interface TarifDao {
    @Query("SELECT * FROM Tarif")
    fun gelAll() : List<Tarif>

    @Query("SELECT * FROM Tarif WHERE id = :id")
    fun findById(id : Int) : Tarif

    @Insert
    fun insert(tarif : Tarif)

    @Delete
    fun delete(tarif : Tarif)
}