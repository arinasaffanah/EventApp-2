package com.dicoding.eventapp.ui.finished

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.eventapp.data.EventRepository
import com.dicoding.eventapp.data.local.room.EventDatabase
import com.dicoding.eventapp.data.remote.response.ListEventsItem
import com.dicoding.eventapp.data.remote.retrofit.ApiConfig
import com.dicoding.eventapp.databinding.FragmentFinishedBinding
import com.dicoding.eventapp.ui.EventAdapter
import com.dicoding.eventapp.ui.MainViewModel
import com.dicoding.eventapp.ui.MainViewModelFactory
import com.dicoding.eventapp.ui.detail.EventDetailActivity
import com.dicoding.eventapp.utils.AppExecutors
import com.google.android.material.snackbar.Snackbar

class FinishedFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private var _binding: FragmentFinishedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFinishedBinding.inflate(inflater, container, false)

        // Inisialisasi ApiService, EventDao, dan AppExecutors
        val apiService = ApiConfig.getApiService()
        val database = EventDatabase.getInstance(requireContext())
        val eventDao = database.eventDao()
        val appExecutors = AppExecutors()

        // Inisialisasi EventRepository
        val repository = EventRepository(apiService, eventDao, appExecutors)

        // Inisialisasi MainViewModel dengan ViewModelFactory
        val factory = MainViewModelFactory(repository)
        mainViewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        setupRecyclerView()

        // Mengamati LiveData untuk daftar acara
        mainViewModel.finishedEventList.observe(viewLifecycleOwner) { eventResponse ->
            eventResponse?.listEvents?.let {
                setEventData(it)
            }
        }

        mainViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        mainViewModel.snackBar.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { snackBarText ->
                Snackbar.make(binding.root, snackBarText, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Mendapatkan acara yang telah selesai
        mainViewModel.getPastEvents()

        return binding.root
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvFinishedEvents.layoutManager = layoutManager
    }

    private fun setEventData(events: List<ListEventsItem>) {
        val adapter = EventAdapter<ListEventsItem>()
        adapter.submitList(events)
        Log.d("FinishedFragment", "Data telah disubmit ke adapter: $events")  // Cek apakah data dikirim
        adapter.setOnItemClickListener(object : EventAdapter.OnItemClickListener<ListEventsItem> {
            override fun onItemClick(event: ListEventsItem) {
                val intent = Intent(requireContext(), EventDetailActivity::class.java)
                intent.putExtra("eventId", event.id.toString())
                startActivity(intent)
            }
        })

        binding.rvFinishedEvents.adapter = adapter
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Menghindari memory leak
    }
}