package dev.hakob.weatherapp.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.hakob.weatherapp.data.response.CurrentWeatherResponse
import dev.hakob.weatherapp.data.response.Temperature

@Entity(
    tableName = "user_weather"
)
data class UserWeatherEntity(
    @PrimaryKey
    val cityId: Int,
    val cityName: String,
    @Embedded val temperature: Temperature,
    val sortOrder: Int = 0
) {
    companion object {
        fun createFromResponse(dto: CurrentWeatherResponse): UserWeatherEntity {
            return UserWeatherEntity(
                dto.id,
                dto.name,
                dto.main!!
            )
        }
    }

}