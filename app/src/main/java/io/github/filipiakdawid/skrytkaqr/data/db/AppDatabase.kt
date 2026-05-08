package io.github.filipiakdawid.skrytkaqr.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.filipiakdawid.skrytkaqr.data.model.Parcel

@Database(entities = [Parcel::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "skrytkaqr.db")
                    .build()
                    .also { instance = it }
            }
    }
}
