package com.dicoding.eventapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.eventapp.R
import com.dicoding.eventapp.data.local.entity.EventEntity
import com.dicoding.eventapp.databinding.ItemEventBinding
import com.dicoding.eventapp.data.remote.response.ListEventsItem

class EventAdapter<T : Any> : ListAdapter<T, EventAdapter.EventViewHolder>(DIFF_CALLBACK as DiffUtil.ItemCallback<T>) {

    private var onItemClickListener: OnItemClickListener<T>? = null

    interface OnItemClickListener<T> {
        fun onItemClick(item: T)
    }

    fun setOnItemClickListener(listener: OnItemClickListener<T>) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(item)
        }
    }

    class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Any) {
            when (item) {
                is ListEventsItem -> {
                    binding.textEventName.text = item.name
                    Glide.with(binding.imageEvent.context).load(item.mediaCover).into(binding.imageEvent)
                }
                is EventEntity -> {
                    binding.textEventName.text = item.name
                    Glide.with(binding.imageEvent.context).load(item.mediaCover).into(binding.imageEvent)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is ListEventsItem && newItem is ListEventsItem -> oldItem.id == newItem.id
                    oldItem is EventEntity && newItem is EventEntity -> oldItem.id == newItem.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem == newItem
            }
        }
    }
}