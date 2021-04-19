package com.example.vaultapplication.database

import androidx.lifecycle.LiveData
import androidx.room.*

import com.example.vaultapplication.model.Sector

@Dao
interface SectorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSector(sector : Sector)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSector(sector: Sector)

    @Query("Select * from Sector order by sector_name")
    fun getSector() : LiveData<List<Sector>>

    @Query("Select * from Sector order by sector_name")
    fun getSectorBackup() : List<Sector>

}