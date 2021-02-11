package dev.hakob.weatherapp.core

import dev.hakob.weatherapp.network.ConnectivityManager

sealed class Resource<out T>(
    val data: T? = null,
    val message: String? = null,
    val networkState: ConnectivityManager.NetworkState? = null
) {
    class Success<T>(data: T, state: ConnectivityManager.NetworkState) : Resource<T>(data = data, networkState = state, message = null)
    class Loading<T>(data: T? = null, state: ConnectivityManager.NetworkState? = null) : Resource<T>(data = data, networkState = state, message = null)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data = data, message = message, networkState = null)
}
