package dev.hakob.weatherapp.data

import dev.hakob.weatherapp.Const.PAGE_SIZE
import dev.hakob.weatherapp.api.WeatherApi
import dev.hakob.weatherapp.core.Resource
import dev.hakob.weatherapp.core.Status
import dev.hakob.weatherapp.data.entity.CityWeather
import dev.hakob.weatherapp.di.AppScope
import dev.hakob.weatherapp.network.ConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


class WeatherRepository @Inject constructor(
    @AppScope private val coroutineScope: CoroutineScope,
    private val weatherDao: WeatherDao,
    private val connectivityManager: ConnectivityManager,
    private val api: WeatherApi
) {

    init {
        // refresh weathers for stored cities
        refreshCityWeathers()
    }

    private fun refreshCityWeathers() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val cityIds = weatherDao.getAllCityIds()
                val queryParam = cityIds.joinToString(",")
                val response = api.getWeatherForCityList(queryParam)
                if (response.isSuccessful) {
                    val bulk = response.body()!!
                    val entities = bulk.list.map { CityWeather.createFromResponse(it) }
                    val updatedEntitiesWithSortOrder = entities.map {
                        val sortOrder = weatherDao.getSortOrderForCityId(it.cityId)!!
                        it.copy(sortOrder = sortOrder)
                    }
                    weatherDao.insertWeathers(updatedEntitiesWithSortOrder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getWeatherList(page: Int): Flow<Resource<List<CityWeather>>> {
        // simple pagination
        val limit = page * PAGE_SIZE
        // combine with network status
        return combine(
            weatherDao.getAllCitiesWithWeather(limit),
            connectivityManager.networkState
        ) { data: List<CityWeather>, networkState: ConnectivityManager.NetworkState ->
            Resource.Success(data, networkState)
        }
    }

    suspend fun addCityWithName(cityName: String): Status = withContext(Dispatchers.IO) {
        try {
            val response = api.getWeatherWithCityName(cityName)
            if (!response.isSuccessful) {
                return@withContext Status.Fail
            }

            // if we have this city already get the stored sortOrder otherwise just take the max value and add 1.
            val sortOrder = weatherDao.getSortOrderForCityId(response.body()!!.id)
                ?: ((weatherDao.maxSortOrder() ?: -1) + 1)

            val entity = CityWeather.createFromResponse(
                response.body() ?: return@withContext Status.Fail
            )
            val sortedEntity = entity.copy(sortOrder = sortOrder)
            weatherDao.insertWeather(sortedEntity)
            return@withContext Status.Success
        } catch (e: Exception) {
            return@withContext Status.Fail
        }
    }

    fun removeCity(city: CityWeather) {
        coroutineScope.launch(Dispatchers.IO) {
            // remove city and reorder
            weatherDao.deleteCityWithIdAndReorder(city.cityId, city.sortOrder)
        }
    }

    fun updateCitySortOrder(item: CityWeather, fromPos: Int, toPos: Int) {
        if (fromPos == toPos) {
            throw IllegalArgumentException("Item $item hasn't moved")
        }
        coroutineScope.launch(Dispatchers.IO) {
            Timber.d("From: $fromPos To: $toPos")
            if (fromPos > toPos) {
                weatherDao.updateSortWhenMovedUp(item.cityId, fromPos, toPos)
            } else {
                weatherDao.updateSortWhenMovedDown(item.cityId, fromPos, toPos)
            }
        }
    }
}