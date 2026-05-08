package io.github.filipiakdawid.skrytkaqr.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.filipiakdawid.skrytkaqr.data.model.Parcel
import io.github.filipiakdawid.skrytkaqr.data.model.ParcelStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcels WHERE status = :status ORDER BY smsDate DESC")
    fun getByStatus(status: ParcelStatus): Flow<List<Parcel>>

    @Query("SELECT * FROM parcels WHERE id = :id")
    suspend fun getById(id: Long): Parcel?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(parcel: Parcel): Long

    @Update
    suspend fun update(parcel: Parcel)

    @Query("DELETE FROM parcels WHERE status = 'TRASH' AND trashedAt < :olderThan")
    suspend fun purgeOldTrash(olderThan: Long)

    @Query("SELECT * FROM parcels WHERE parcelNumber = :number LIMIT 1")
    suspend fun getByParcelNumber(number: String): Parcel?

    @Query("SELECT * FROM parcels WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): Parcel?

    @Delete
    suspend fun delete(parcel: Parcel)
}
