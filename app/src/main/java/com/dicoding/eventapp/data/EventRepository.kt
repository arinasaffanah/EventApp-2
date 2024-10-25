package com.dicoding.eventapp.data


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dicoding.eventapp.data.local.entity.EventEntity
import com.dicoding.eventapp.data.local.room.EventDao
import com.dicoding.eventapp.data.remote.retrofit.ApiService
import com.dicoding.eventapp.utils.AppExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepository(
    private val apiService: ApiService,
    private val eventDao: EventDao,
    private val appExecutors: AppExecutors
) {
    private val result = MutableLiveData<Result<List<EventEntity>>>()

    suspend fun getActiveEvents(): LiveData<Result<List<EventEntity>>> {
        result.value = Result.Loading
        try {
            // Memanggil API untuk mendapatkan event aktif
            val response = apiService.getActiveEvents() // Mengambil event aktif dari API
            val events = response.listEvents // Mengakses listEvents

            // Pindahkan operasi database ke dalam coroutine
            withContext(Dispatchers.IO) {
                // Konversi List<ListEventsItem> ke List<EventEntity>
                val eventEntities = events.map { eventItem ->
                    EventEntity(
                        id = eventItem.id ?: 0,
                        name = eventItem.name ?: "No Name",
                        beginTime = eventItem.beginTime ?: "No Begin Time",
                        endTime = eventItem.endTime ?: "No End Time",
                        mediaCover = eventItem.mediaCover,
                        isFavorite = eventItem.isFavorite
                    )
                }

                // Menghapus semua event sebelumnya dan menambahkan yang baru
                eventDao.deleteAll() // Menghapus semua event
                eventDao.insertEvents(eventEntities) // Ganti insertEvent dengan insertEvents

                // Memposting hasil ke LiveData setelah menyimpan data
                result.postValue(Result.Success(eventEntities))
            }
        } catch (t: Throwable) {
            result.value = Result.Error(t.message.toString())
        }

        return result
    }

    fun getFavoriteEvents(): LiveData<List<EventEntity>> {
        return eventDao.getFavoriteEvents() // EventDao harus mengembalikan LiveData
    }

    suspend fun addEventToFavorites(event: EventEntity) {
        withContext(Dispatchers.IO) {
            eventDao.insertEvent(event)
        }
    }

    suspend fun removeEventFromFavorites(eventId: Int) {
        withContext(Dispatchers.IO) {
            eventDao.deleteEvent(eventId) // Memanggil fungsi suspend dalam coroutine
        }
    }
}