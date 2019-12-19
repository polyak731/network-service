package com.example.networkchangesample.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkUtils {

    @Suppress("DEPRECATION")
    fun checkNetworkState(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.connectivityManager.allNetworks.forEach {
                context.connectivityManager.getNetworkCapabilities(it)?.let { capabilities ->
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    }
                }
            }
            return false
        } else {
            context.connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

    val Context.connectivityManager
        get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}