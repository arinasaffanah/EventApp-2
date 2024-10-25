package com.dicoding.eventapp.di

import android.content.Context
import com.dicoding.eventapp.data.EventRepository
import com.dicoding.eventapp.data.local.room.EventDatabase
import com.dicoding.eventapp.data.remote.retrofit.ApiConfig
import com.dicoding.eventapp.utils.AppExecutors

object Injection {
    fun provideRepository(context: Context): EventRepository {
        // Mendapatkan instance ApiService
        val apiService = ApiConfig.getApiService()

        // Mendapatkan instance EventDatabase dan EventDao
        val database = EventDatabase.getInstance(context)
        val dao = database.eventDao()

        // Jika menggunakan AppExecutors untuk manajemen thread, sesuaikan sesuai kebutuhan
        val appExecutors = AppExecutors()

        // Mengembalikan instance EventRepository
        return EventRepository(apiService, dao, appExecutors)
    }
}