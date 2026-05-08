package io.github.filipiakdawid.skrytkaqr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ParcelStatus { ACTIVE, TRASH }

@Entity(tableName = "parcels")
data class Parcel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // "905432"
    val code: String,
    val parcelNumber: String,
    // "WWA35M" Warszawa
    val location: String,
    // "16/04/2026"
    val expiryDate: String,
    // "09:31"
    val expiryTime: String,
    // sms timestamp
    val smsDate: Long,
    val importedAt: Long = System.currentTimeMillis(),
    val status: ParcelStatus = ParcelStatus.ACTIVE,
    val trashedAt: Long? = null,
    val parcelCount: Int = 1,
)
