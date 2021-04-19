package com.example.vaultapplication.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vaultapplication.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addUser(user  :User)

    @Query("SELECT * FROM User WHERE mobile_number=:mobileNumber")
    fun getUser(mobileNumber : Long) : User?
}