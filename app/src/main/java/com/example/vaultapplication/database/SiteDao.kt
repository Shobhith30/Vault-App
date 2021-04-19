package com.example.vaultapplication.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.vaultapplication.SectorCount
import com.example.vaultapplication.model.Site

@Dao
interface SiteDao {

    @Insert
    fun addSite(site : Site)

    @Update
    fun updateSite(site: Site)

    @Query("Select * FROM Site order by timestamp desc")
    fun getSite()  :List<Site>

    @Query("Select * from Site where sector=:sector order by timestamp desc")
    fun getSiteBySector(sector:String) : LiveData<List<Site>>

    @Query("Select sector as sectorName,COUNT(*) as sectorCount from Site group by sector")
    fun getSectorCount() : LiveData<List<SectorCount>>

    @Query("Select MAX(id) from Site")
    fun getId(): Int

}