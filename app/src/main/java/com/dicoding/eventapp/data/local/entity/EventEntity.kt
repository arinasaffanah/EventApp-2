package com.dicoding.eventapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "beginTime")
    val beginTime: String,

    @ColumnInfo(name = "endTime")
    val endTime: String,

    @ColumnInfo(name = "mediaCover")
    val mediaCover: String? = null,

    @ColumnInfo(name = "isFavorite")
    var isFavorite: Boolean = false
)