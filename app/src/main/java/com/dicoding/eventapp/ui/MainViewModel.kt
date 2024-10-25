package com.dicoding.eventapp.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.eventapp.data.remote.response.EventResponse
import com.dicoding.eventapp.data.remote.retrofit.ApiConfig
import com.dicoding.eventapp.utils.Events
import kotlinx.coroutines.launch
import com.dicoding.eventapp.data.remote.response.ListEventsItem
import kotlinx.coroutines.async
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.dicoding.eventapp.data.EventRepository
import com.dicoding.eventapp.data.local.entity.EventEntity



class MainViewModel(private val repository: EventRepository) : ViewModel() {
    private val _eventList = MutableLiveData<EventResponse>()
    val eventList: LiveData<EventResponse?> get() = _eventList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _snackBar = MutableLiveData<Events<String>>()
    val snackBar: LiveData<Events<String>> get() = _snackBar

    private val _eventDetails = MutableLiveData<ListEventsItem?>()
    val eventDetails: LiveData<ListEventsItem?> get() = _eventDetails

    private val _finishedEventList = MutableLiveData<EventResponse>()
    val finishedEventList: LiveData<EventResponse> get() = _finishedEventList

    private val _upcomingEventList = MutableLiveData<EventResponse>()
    val upcomingEventList: LiveData<EventResponse> get() = _upcomingEventList

    companion object {
        private const val TAG = "MainViewModel"
    }

    fun getActiveEvents() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().getActiveEvents()
                _eventList.value = response
                _upcomingEventList.value = response
            } catch (e: Exception) {
                _snackBar.value = Events("Terjadi kesalahan: ${e.message}")
                Log.e(TAG, "Error fetching active events: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getPastEvents() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().getPastEvents()
                _finishedEventList.value = response
            } catch (e: Exception) {
                _snackBar.value = Events("Terjadi kesalahan saat mengambil data.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAllEvents() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val activeEventsDeferred = async { ApiConfig.getApiService().getActiveEvents() }
                val pastEventsDeferred = async { ApiConfig.getApiService().getPastEvents() }

                val activeEventsResponse = activeEventsDeferred.await()
                val pastEventsResponse = pastEventsDeferred.await()

                _upcomingEventList.value = activeEventsResponse
                _finishedEventList.value = pastEventsResponse
                _eventList.value = EventResponse(
                    listEvents = activeEventsResponse.listEvents + pastEventsResponse.listEvents
                )
            } catch (e: Exception) {
                _snackBar.value = Events("Terjadi kesalahan: ${e.message}")
                Log.e(TAG, "Error fetching events: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addEventToFavorites(event: EventEntity) {
        viewModelScope.launch {
            try {
                repository.addEventToFavorites(event)
                _snackBar.value = Events("Event berhasil ditambahkan ke favorit")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding event to favorites: ${e.message}")
                _snackBar.value = Events("Gagal menambahkan event ke favorit")
            }
        }
    }

    fun getFavoriteEvents(): LiveData<List<EventEntity>> {
        return repository.getFavoriteEvents()
    }
}