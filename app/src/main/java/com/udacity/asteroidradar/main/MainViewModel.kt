package com.udacity.asteroidradar.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.launch


class MainViewModel(application: Application) : ViewModel() {

    private val _asteroids = MutableLiveData<List<Asteroid>>()

    private val database = getDatabase(application)

    val asteroids: LiveData<List<Asteroid>>
    get() = _asteroids

    private val asteroidsRepository = AsteroidsRepository(database)

    init {
        viewModelScope.launch {
            asteroidsRepository.refreshAsteroids()
        }
    }

    val asteroidRetrievedList = asteroidsRepository.asteroids

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid>()

    val navigateToSelectedAsteroid: LiveData<Asteroid>
    get() = _navigateToSelectedAsteroid

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedAsteroid.value = null
    }


}