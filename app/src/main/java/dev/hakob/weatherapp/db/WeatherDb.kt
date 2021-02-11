package dev.hakob.weatherapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.hakob.weatherapp.data.WeatherDao
import dev.hakob.weatherapp.data.entity.UserWeatherEntity

@Database(
    entities =
    [
        UserWeatherEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WeatherDb : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao
}