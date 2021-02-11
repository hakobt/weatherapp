package dev.hakob.weatherapp.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.*
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.hakob.weatherapp.di.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityManager @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    @AppScope
    private val scope: CoroutineScope
) {

    private val networkStateFlow = MutableStateFlow(NetworkState.UNKNOWN)

    // simple network state holder
    val networkState: StateFlow<NetworkState> = networkStateFlow

    init {
        val networkManager = appContext.getSystemService<ConnectivityManager>()!!

        val currentNetwork = networkManager.activeNetwork
        val isConnected = networkManager.getNetworkCapabilities(currentNetwork)
            ?.hasCapability(NET_CAPABILITY_INTERNET)

        if (isConnected == null || isConnected == false) {
            updateNetworkState(NetworkState.DISCONNECTED)
        }

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(TRANSPORT_CELLULAR)
            .addTransportType(TRANSPORT_WIFI)
            .build()

        networkManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onUnavailable() {
                    updateNetworkState(NetworkState.DISCONNECTED)
                }

                override fun onAvailable(network: Network) {
                    updateNetworkState(NetworkState.CONNECTED)
                }

                override fun onLost(network: Network) {
                    updateNetworkState(NetworkState.DISCONNECTED)
                }
            }
        )
    }

    private fun updateNetworkState(state: NetworkState) {
        scope.launch(Dispatchers.Default) {
            networkStateFlow.emit(state)
        }
    }

    enum class NetworkState {
        UNKNOWN, CONNECTED, DISCONNECTED
    }
}