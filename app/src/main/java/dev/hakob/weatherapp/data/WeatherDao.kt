package dev.hakob.weatherapp.data

import androidx.room.*
import dev.hakob.weatherapp.data.entity.UserWeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM user_weather WHERE cityId = :cityId LIMIT 1")
    fun getWeatherForCityWithId(cityId: Int): Flow<UserWeatherEntity>

    @Query("SELECT * FROM user_weather ORDER BY sortOrder ASC LIMIT :limit ")
    fun getAllCitiesWithWeather(limit: Int): Flow<List<UserWeatherEntity>>

    @Query("SELECT cityId FROM user_weather")
    suspend fun getAllCityIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWeather(userWeatherEntity: UserWeatherEntity)

    @Update
    fun insertWeathers(list: List<UserWeatherEntity>)

    @Query("DELETE FROM user_weather WHERE cityId = :cityId")
    fun deleteCityWithId(cityId: Int)

    @Query("SELECT MAX(sortOrder) FROM user_weather")
    suspend fun maxSortOrder(): Int?

    @Query("UPDATE user_weather SET sortOrder = sortOrder + 1 WHERE sortOrder < :fromPos AND sortOrder >= :toPos")
    suspend fun updateSortWhenMovedUpInternal(fromPos: Int, toPos: Int)

    @Query("UPDATE user_weather SET sortOrder = sortOrder - 1 WHERE sortOrder > :fromPos AND sortOrder <= :toPos")
    suspend fun updateSortWhenMovedDownInternal(fromPos: Int, toPos: Int)

    @Query("UPDATE user_weather SET sortOrder=:newPos WHERE cityId =:cityId")
    suspend fun updateCurrent(cityId: Int, newPos: Int)

    @Transaction
    suspend fun updateSortWhenMovedDown(cityId: Int, fromPos: Int, toPos: Int) {
        updateSortWhenMovedDownInternal(fromPos, toPos)
        updateCurrent(cityId, toPos)
    }

    @Transaction
    suspend fun updateSortWhenMovedUp(cityId: Int, fromPos: Int, toPos: Int) {
        updateSortWhenMovedUpInternal(fromPos, toPos)
        updateCurrent(cityId, toPos)
    }

    @Transaction
    suspend fun deleteCityWithIdAndReorder(cityId: Int, sortOrder: Int) {
        deleteCityWithId(cityId)
        reorderSortOrderFrom(sortOrder)
    }

    @Query("UPDATE user_weather SET sortOrder = sortOrder - 1 WHERE sortOrder > :sortOrder")
    suspend fun reorderSortOrderFrom(sortOrder: Int)

    @Query("SELECT sortOrder FROM user_weather WHERE cityId = :cityId")
    suspend fun getSortOrderForCityId(cityId: Int): Int?
}