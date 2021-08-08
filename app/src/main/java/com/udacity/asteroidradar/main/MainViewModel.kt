package com.udacity.asteroidradar.main

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.NetworkMoshi
import com.udacity.asteroidradar.api.apiKey
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import com.udacity.asteroidradar.database.getDatabase
import java.net.ConnectException
import java.net.SocketTimeoutException


@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(application: Application) : ViewModel() {

    var picUrl2: String = "https://square.github.io/picasso/static/sample.png"

    private val _asteroids = MutableLiveData<List<Asteroid>>()

    private val database = getDatabase(application)

    var imageTitle = ""

    val asteroids: LiveData<List<Asteroid>>

    get() = _asteroids

    private val _readyToDownloadPicasso = MutableLiveData<Boolean>()

    val readyToDownloadPicasso: LiveData<Boolean>
    get() = _readyToDownloadPicasso

    fun timeToDownloadPicasso() {
        _readyToDownloadPicasso.value = true
    }

    fun notTimeToDownloadPicasso() {
        _readyToDownloadPicasso.value = false
    }

    private val asteroidsRepository = AsteroidsRepository(database)

    init {
        viewModelScope.launch {
            asteroidsRepository.refreshAsteroids()
        }
    }



    var asteroidRetrievedList: MutableLiveData<List<Asteroid>> = asteroidsRepository.weekAsteroids as MutableLiveData<List<Asteroid>>

    fun chooseList(int: Int) {
        asteroidRetrievedList = when (int) {
        1 -> asteroidsRepository.weekAsteroids as MutableLiveData<List<Asteroid>>
            2 -> asteroidsRepository.todaysAsteroids as MutableLiveData<List<Asteroid>>
            3 -> asteroidsRepository.allSavedAsteroids as MutableLiveData<List<Asteroid>>
            else -> asteroidsRepository.weekAsteroids as MutableLiveData<List<Asteroid>>
    }
    }

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid>()

    val navigateToSelectedAsteroid: LiveData<Asteroid>
    get() = _navigateToSelectedAsteroid

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedAsteroid.value = null
    }

/*
    suspend fun refreshPhotoOfTheDay() : String {
        withContext(Dispatchers.IO)
        {
            val urlString = " https://api.nasa.gov/planetary/apod?api_key=" + apiKey.API_KEY
            val temp = NetworkMoshi.asterMoshi.getPhotoData().await()
            picUrl2 = temp.urlString
        }
        return picUrl2
    }
 */


    fun refreshPhotoOfTheDay()  {
        viewModelScope.launch {
            try {
                val temp = NetworkMoshi.asterMoshi.getPhotoData().await()
                picUrl2 = temp.url
                imageTitle = temp.title
                timeToDownloadPicasso()
            } catch (e: Exception) {
                timeToDownloadPicasso()
                Log.i("CHARLES: Exception", "$e.printStackTrace()")
            }
        }
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }



}