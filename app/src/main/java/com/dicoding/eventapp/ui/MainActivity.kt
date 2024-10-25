package com.dicoding.eventapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.eventapp.R
import com.dicoding.eventapp.data.EventRepository
import com.dicoding.eventapp.data.local.room.EventDatabase
import com.dicoding.eventapp.data.remote.response.ListEventsItem
import com.dicoding.eventapp.data.remote.retrofit.ApiConfig
import com.dicoding.eventapp.databinding.ActivityMainBinding
import com.dicoding.eventapp.ui.detail.EventDetailActivity
import com.dicoding.eventapp.utils.AppExecutors
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Inisialisasi ApiService dari ApiConfig
        val apiService = ApiConfig.getApiService()

        // Inisialisasi EventDatabase dan EventDao
        val database = EventDatabase.getInstance(applicationContext)
        val eventDao = database.eventDao()

        // Inisialisasi AppExecutors
        val appExecutors = AppExecutors()

        // Inisialisasi EventRepository
        val repository = EventRepository(apiService, eventDao, appExecutors)

        // Gunakan ViewModelFactory untuk inisialisasi MainViewModel
        val factory = MainViewModelFactory(repository)
        mainViewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        setupRecyclerView()
        setupNavigation()

        // Observe LiveData dari ViewModel
        mainViewModel.eventList.observe(this) { eventResponse ->
            eventResponse?.listEvents?.let {
                setEventData(it)
            } ?: Log.e("MainActivity", "No events found")
        }

        mainViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        mainViewModel.snackBar.observe(this) {
            it.getContentIfNotHandled()?.let { snackBarText ->
                Snackbar.make(window.decorView.rootView, snackBarText, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvEvents.layoutManager = layoutManager
        binding.rvEvents.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_upcoming, R.id.navigation_finished)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun setEventData(events: List<ListEventsItem>) {
        val adapter = EventAdapter<ListEventsItem>()
        adapter.submitList(events)
        adapter.setOnItemClickListener(object : EventAdapter.OnItemClickListener<ListEventsItem> {
            override fun onItemClick(event: ListEventsItem) {
                val intent = Intent(this@MainActivity, EventDetailActivity::class.java)
                intent.putExtra("eventId", event.id.toString())
                startActivity(intent)
            }
        })

        binding.rvEvents.adapter = adapter
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}