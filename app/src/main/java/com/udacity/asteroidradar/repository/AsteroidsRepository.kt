package com.udacity.asteroidradar.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.NetworkScalar
import com.udacity.asteroidradar.api.apiKey
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.main.MainFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AsteroidsRepository(private val database: AsteroidsDatabase) {

    private val myBaseUrl = "https://api.nasa.gov/neo/rest/v1/"

    init {

    }

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
            } catch (e: Exception) {
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