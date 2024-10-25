    package com.dicoding.eventapp.ui

    import android.content.Context
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.ViewModelProvider
    import com.dicoding.eventapp.data.EventRepository
    import com.dicoding.eventapp.di.Injection

    class MainViewModelFactory(private val repository: EventRepository) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }

        companion object {
            @Volatile
            private var instance: MainViewModelFactory? = null
            fun getInstance(context: Context): MainViewModelFactory =
                instance ?: synchronized(this) {
                    instance ?: MainViewModelFactory(Injection.provideRepository(context))
                }.also { instance = it }
        }
    }