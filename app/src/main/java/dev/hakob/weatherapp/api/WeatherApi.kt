package dev.hakob.weatherapp.api

import dev.hakob.weatherapp.data.response.BulkWeatherResponse
import dev.hakob.weatherapp.data.response.CurrentWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("weather")
    suspend fun getWeatherWithLatLng(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<CurrentWeatherResponse>

    @GET("weather")
    suspend fun getWeatherWithCityName(
        @Query("q") cityName: String
    ): Response<CurrentWeatherResponse>

    @GET("group")
    suspend fun getWeatherForCityList(
            @Query("id") ids: String
    ): Response<BulkWeatherResponse>
}