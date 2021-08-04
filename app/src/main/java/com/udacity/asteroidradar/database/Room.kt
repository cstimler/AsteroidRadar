package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {
    @Query("select * from databaseasteroids where date(closeApproachDate) >= date() order by date(closeApproachDate)")
    fun getAsteroids(): LiveData<List<DatabaseAsteroids>>

    @Insert(onConflict=OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroids)
}

@Database(entities = [DatabaseAsteroids::class], version = 1)
abstract class AsteroidsDatabase: RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidsDatabase

fun getDatabase(context: Context): AsteroidsDatabase {
    synchronized(AsteroidsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
            AsteroidsDatabase::class.java,
            "asteroids").build()
        }
    }
    return INSTANCE
}