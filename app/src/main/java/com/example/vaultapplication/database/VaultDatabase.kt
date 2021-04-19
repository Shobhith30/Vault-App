package com.example.vaultapplication.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vaultapplication.R
import com.example.vaultapplication.model.Sector
import com.example.vaultapplication.model.Site
import com.example.vaultapplication.model.User


@Database(entities = [User::class,Sector::class,Site::class], version = 1,exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {

    abstract fun userDao() : UserDao
    abstract fun sectorDao()  :SectorDao
    abstract fun siteDao()  :SiteDao

    companion object {

        @Volatile private var databaseInstance: VaultDatabase? = null

        @Synchronized
        fun getDatabaseInstance(context: Context): VaultDatabase {

            val callback = object : RoomDatabase.Callback(){
                override fun onCreate(db: SupportSQLiteDatabase) {
                    val sectorArr : Array<String> = context.resources.getStringArray(R.array.sector)
                        for(sector in sectorArr)
                            db.execSQL("INSERT into Sector(sector_name) values('$sector')")
                }
            }

            if (databaseInstance == null) {
                databaseInstance = Room.databaseBuilder(
                        context.applicationContext,
                        VaultDatabase::class.java,
                        "test8_db"
                ).addCallback(callback).build()
            }

            return databaseInstance!!
        }


    }
}