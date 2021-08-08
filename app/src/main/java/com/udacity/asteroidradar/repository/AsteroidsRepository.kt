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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AsteroidsRepository(private val database: AsteroidsDatabase) {

    //public val startDate = Date()
    //public val endDate = Date()
    private val myBaseUrl = "https://api.nasa.gov/neo/rest/v1/"

  //  val urlString = myBaseUrl + "feed?start_date=" + startDate.toString() + "&end_date=" + endDate.toString() +
  //          "&api_key=" + apiKey.API_KEY
    // val urlStringTemp = myBaseUrl + "feed?start_date=2021-08-06" + "&end_date=2021-08-13" + "&api_key=" + apiKey.API_KEY

    val weekAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroids()) {
        it.asDomainModel()
    }

    val todaysAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getTodaysAsteroids()) {
        it.asDomainModel()
    }

    val allSavedAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAllSavedAsteroids()) {
        it.asDomainModel()
    }

 //   val asteroidlist:
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO)
        {
            val urlStringTemp = getUrlStringTemp()
          //  Log.i("CHARLES: Url String", urlStringTemp)
            val temp = NetworkScalar.asterScalar.getAsteroidsFromNetwork(urlStringTemp).await()
           // val temp2 = temp.near_earth_objects
          //  Log.i("CHARLES: String", temp)
            val temp3 = JSONObject(temp)
            // Log.i("CHARLES: JSON", temp3.toString())
            val asteroidlist = parseAsteroidsJsonResult(temp3)
            Log.i("CHARLES: LIST", asteroidlist.toString())
            database.asteroidDao.insertAll(*AsteroidContainer(asteroidlist).asDatabaseModel())
        }
    }





// from https://www.programiz.com/kotlin-programming/examples/current-date-time

        @RequiresApi(Build.VERSION_CODES.O)
        fun getUrlStringTemp() : String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDate = current.format(formatter)
        val sevenDaysFromNow = current.plusDays(7)
        val endDate = sevenDaysFromNow.format(formatter)
        Log.i("CHARLES - Check date format", "Current Date and Time is: $startDate and $endDate")
        return myBaseUrl + "feed?start_date=" + startDate.toString() + "&end_date=" + endDate.toString() +
            "&api_key=" + apiKey.API_KEY
    }
}