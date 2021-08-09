package com.udacity.asteroidradar.database

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {
    @Query("select * from databaseasteroids where date(closeApproachDate) >= date() order by date(closeApproachDate)")
    fun getAsteroids(): LiveData<List<DatabaseAsteroids>>

    @Insert(onConflict=OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroids)

    @Query("select * from databaseasteroids where date(closeApproachDate) = date()")
    fun getTodaysAsteroids(): LiveData<List<DatabaseAsteroids>>

    @Query("select * from databaseasteroids order by date(closeApproachDate)")
    fun getAllSavedAsteroids(): LiveData<List<DatabaseAsteroids>>
}

@Database(entities = [DatabaseAsteroids::class], version = 1)
abstract class AsteroidsDatabase: RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidsDatabase

fun getDatabase(application: Application): AsteroidsDatabase {
    synchronized(AsteroidsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(application.applicationContext,
            AsteroidsDatabase::class.java,
            "asteroids").build()
        }
    }
    return INSTANCE
}