package com.example.vaultapplication.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude

@Entity(indices = [Index(value = ["sector_name"], unique = true)])
data class Sector(
    @PrimaryKey(autoGenerate = true)
    private val id : Int = 0,
    @ColumnInfo(name = "sector_name")
    private val sectorName : String = "") {

    @Exclude fun getId() = id
    fun getSectorName() = sectorName
}