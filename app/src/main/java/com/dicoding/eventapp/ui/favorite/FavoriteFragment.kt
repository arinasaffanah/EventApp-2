package com.dicoding.eventapp.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.eventapp.data.local.entity.EventEntity
import com.dicoding.eventapp.data.remote.response.ListEventsItem
import com.dicoding.eventapp.databinding.FragmentFavoriteBinding
import com.dicoding.eventapp.ui.EventAdapter
import com.dicoding.eventapp.ui.MainViewModel
import com.dicoding.eventapp.ui.detail.EventDetailActivity
import com.google.android.material.snackbar.Snackbar

class FavoriteFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val adapter = EventAdapter<ListEventsItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)

        // Setup RecyclerView
        binding.rvFavoriteEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavoriteEvents.adapter = adapter

        // Inisialisasi ViewModel
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        // Mengamati LiveData untuk daftar acara favorit
        mainViewModel.getFavoriteEvents().observe(viewLifecycleOwner) { favoriteEvents ->
            if (favoriteEvents.isNotEmpty()) {
                setEventData(favoriteEvents)
                binding.progressBar.visibility = View.GONE
                binding.rvFavoriteEvents.visibility = View.VISIBLE
                binding.tvNoFavorites.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.rvFavoriteEvents.visibility = View.GONE
                binding.tvNoFavorites.visibility = View.VISIBLE
            }
        }

        // Mengamati LiveData untuk loading state
        mainViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        // Mengamati LiveData untuk Snackbar
        mainViewModel.snackBar.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { snackBarText ->
                Snackbar.make(binding.root, snackBarText, Snackbar.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun setEventData(events: List<EventEntity>) {
        // Konversi dari EventEntity ke ListEventsItem
        val eventItems = events.map { eventEntity ->
            ListEventsItem(
                id = eventEntity.id,
                name = eventEntity.name,
                mediaCover = eventEntity.mediaCover,
                beginTime = eventEntity.beginTime,
                endTime = eventEntity.endTime
            )
        }

        adapter.submitList(eventItems) // Mengirimkan ListEventsItem ke adapter
        adapter.setOnItemClickListener(object : EventAdapter.OnItemClickListener<ListEventsItem> {
            override fun onItemClick(event: ListEventsItem) {
                val eventId = event.id?.toString()
                if (eventId != null) {
                    val intent = Intent(requireContext(), EventDetailActivity::class.java)
                    intent.putExtra("eventId", eventId)
                    startActivity(intent)
                }
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvFavoriteEvents.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Menghindari memory leak
    }
}