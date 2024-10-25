package com.dicoding.eventapp.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.eventapp.data.local.entity.EventEntity

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Menambahkan fungsi ini
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Int)

    @Query("DELETE FROM events")
    suspend fun deleteAll() // Fungsi untuk menghapus semua event

    @Query("SELECT * FROM events WHERE isFavorite = 1")
    fun getFavoriteEvents(): LiveData<List<EventEntity>>

}