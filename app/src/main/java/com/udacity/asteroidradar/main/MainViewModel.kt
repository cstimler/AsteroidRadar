package com.udacity.asteroidradar.main

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.database.getDatabase
import org.json.JSONObject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(application: Application) : ViewModel() {

    var picUrl2: String = "https://square.github.io/picasso/static/sample.png"

    private val _asteroids = MutableLiveData<List<Asteroid>>()

    private val database = getDatabase(application)

    var imageTitle = ""

    // val asteroids: LiveData<List<Asteroid>>

    // took this out of AsteroidsRepository:

    val weekAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroids()) {
        it.asDomainModel()
    }
    // Log.i("CHARLES in Repository", "getAsteroids")


    val todaysAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getTodaysAsteroids()) {
        it.asDomainModel()
    }


    val allSavedAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAllSavedAsteroids()) {
        it.asDomainModel()
    }



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
       getAsteroidsFromNetworkNow()
        }



    fun getAsteroidsFromNetworkNow() {
        viewModelScope.launch()
        {
            refreshAsteroids()
        }
    }

    private val _dataHasUpdated = MutableLiveData<Boolean>()
    val dataHasUpdated: LiveData<Boolean>
    get() = _dataHasUpdated

    var asteroidRetrievedList2: LiveData<List<Asteroid>>? = todaysAsteroids
    var asteroidRetrievedList: MutableLiveData<List<Asteroid>>? = weekAsteroids as MutableLiveData<List<Asteroid>>
    var asteroidRetrievedList1: LiveData<List<Asteroid>>? = weekAsteroids

    var asteroidRetriedList3: LiveData<List<Asteroid>>? = allSavedAsteroids

    /*
    fun chooseList(int: Int) {
        asteroidRetrievedList?.value = when (int) {
            1 -> asteroidRetrievedList1?.value
            2 -> asteroidRetrievedList2?.value
            3 -> asteroidRetriedList3?.value
            else -> asteroidRetrievedList2?.value
    }
        Log.i("CHARLES int value", int.toString())
        announceDataHasUpdated()
        Log.i("CHARLES BETTER BE variable", asteroidRetrievedList?.value.toString())
        Log.i("CHARLES BETTER BE week", asteroidRetrievedList1?.value.toString())
        Log.i("CHARLES BETTER BE todays", asteroidRetrievedList2?.value.toString())
        Log.i("CHARLES BETTER BE allSaved", asteroidRetriedList3?.value.toString())
        Log.i("CHARLES ANOTHER today's list", asteroidsRepository.todaysAsteroids.value.toString())

        //(source)
    }

     */

    fun announceDataHasUpdated() {
        _dataHasUpdated.value = true
    }

    fun announceDataUpdateIsOver() {
        _dataHasUpdated.value = false
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

    // Moving more stuff in from the AsteroidsRepository:






    fun refreshPhotoOfTheDay()  {
        viewModelScope.launch {
            try {
                Log.i("CHARLES in refresh", "just entered")
                val temp = NetworkMoshi.asterMoshi.getPhotoData().await()
                Log.i("CHARLES in refresh2", "got past val temp")
                picUrl2 = temp.url
                imageTitle = temp.title
                timeToDownloadPicasso()
                Log.i("CHARLES after timeto..", picUrl2)
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





    // Moving stuff in from AsteroidRepository:

    private val myBaseUrl = "https://api.nasa.gov/neo/rest/v1/"

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO)
        {
            val urlStringTemp = getUrlStringTemp()
            Log.i("CHARLES: Url String", urlStringTemp)
            try {
                val temp = NetworkScalar.asterScalar.getAsteroidsFromNetwork(urlStringTemp).await()
                Log.i("CHARLES:", "Loading Asteroids Success")
                val temp1 = JSONObject(temp)
                val asteroidlist = parseAsteroidsJsonResult(temp1)
                database.asteroidDao.insertAll(*AsteroidContainer(asteroidlist).asDatabaseModel())
            } catch (e: java.lang.Exception) {
                Log.i("CHARLES: exception", "Exception when loading asteroids")
                Log.i("CHARLES: refreshAsteroids exception", e.toString())
            }
        }
    }

// some of below adapted from https://www.programiz.com/kotlin-programming/examples/current-date-time

    @RequiresApi(Build.VERSION_CODES.O)
    fun getUrlStringTemp() : String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDate = current.format(formatter)
        val sevenDaysFromNow = current.plusDays(7)
        val endDate = sevenDaysFromNow.format(formatter)
        return myBaseUrl + "feed?start_date=" + startDate.toString() + "&end_date=" + endDate.toString() +
                "&api_key=" + apiKey.API_KEY
    }
}
