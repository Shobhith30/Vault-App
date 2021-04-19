package com.example.vaultapplication.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "mobile_number")
    private val mobileNumber : Long,

    @ColumnInfo(name = "m_pin")
    private val mPin : String) {

    fun getMobileNumber() = mobileNumber
    fun getMPin() = mPin
}