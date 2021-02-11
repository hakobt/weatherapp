package dev.hakob.weatherapp.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor: Interceptor {

    private val key = "appid"
    private val appId = "197b39149c4224a63ee2369fbde75429"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val newHttpUrl = request.url.newBuilder().addQueryParameter(key, appId).build()

        val newRequest = request.newBuilder().url(newHttpUrl).build()

        return chain.proceed(newRequest)
    }
}