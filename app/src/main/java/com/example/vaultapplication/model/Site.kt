package com.example.vaultapplication.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import java.util.*
import org.jetbrains.annotations.NotNull


@Entity(foreignKeys = [ForeignKey(entity = User::class,
    parentColumns = arrayOf("mobile_number"),
    childColumns = arrayOf("mobile_number"),
    onDelete = ForeignKey.CASCADE,
    onUpdate = ForeignKey.CASCADE)], primaryKeys = ["device_identifier","id"]
)
data class Site(
    private val url : String ="",
    @ColumnInfo(name = "site_name")
    private val siteName : String ="",
    private val sector : String ="",
    private val username : String ="",
    private val password : String ="",
    private val notes : String ="",
    @ColumnInfo(name = "mobile_number")
    private val mobileNumber : Long =0L,
    @ColumnInfo(name = "device_identifier")
    @NonNull val deviceIdentifier : String = "",
    private val id : Int = 0,
    private val timestamp : Long = System.currentTimeMillis()) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readLong(),
        parcel.readString().toString(),
        parcel.readInt()
    )

    fun getUrl() = url
    fun getSiteName() = siteName
    fun getSector() = sector
    fun getUsername() = username
    fun getPassword() = password
    fun getNotes() = notes
    fun getMobileNumber()  =mobileNumber
    fun getId() = id
    fun getTimestamp() = timestamp

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(siteName)
        parcel.writeString(sector)
        parcel.writeString(username)
        parcel.writeString(password)
        parcel.writeString(notes)
        parcel.writeLong(mobileNumber)
        parcel.writeString(deviceIdentifier)
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Site> {
        override fun createFromParcel(parcel: Parcel): Site {
            return Site(parcel)
        }

        override fun newArray(size: Int): Array<Site?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        return if(other != null){
            val otherSite = other as Site
            this.getUrl() == otherSite.getUrl() &&
                    this.getSiteName() == otherSite.getSiteName() &&
                    this.getSector() == otherSite.getSector() &&
                    this.getUsername() == otherSite.getUsername() &&
                    this.getPassword() == otherSite.getPassword() &&
                    this.getNotes() == otherSite.getNotes()
        }else
            false
    }


}
