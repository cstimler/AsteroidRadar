package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.NetworkScalar
import com.udacity.asteroidradar.api.apiKey
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*

class AsteroidsRepository(private val database: AsteroidsDatabase) {

    public val startDate = Date()
    public val endDate = Date()
    private val myBaseUrl = "https://api.nasa.gov/neo/rest/v1/"

    val urlString = myBaseUrl + "feed?start_date=" + startDate.toString() + "&end_date=" + endDate.toString() +
            "&api_key=" + apiKey.API_KEY
    val urlStringTemp = myBaseUrl + "feed?start_date=2021-05-05" + "&end_date=2021-05-07" + "&api_key=" + apiKey.API_KEY

    val asteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroids()) {
        it.asDomainModel()
    }

 //   val asteroidlist:
    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            val temp = NetworkScalar.asterScalar.getAsteroidsFromNetwork(urlStringTemp).await()
           // val temp2 = temp.near_earth_objects
            val temp3 = JSONObject(temp)
            val asteroidlist = modifiedParseAsteroidsJsonResult(temp3)
            database.asteroidDao.insertAll(*AsteroidContainer(asteroidlist).asDatabaseModel())
        }
    }
}