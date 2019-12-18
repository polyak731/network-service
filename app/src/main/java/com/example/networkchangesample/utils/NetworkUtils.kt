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

    @Suppress("DEPRECATION")
    fun checkWifiState(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.connectivityManager.allNetworks.forEach {
                context.connectivityManager.getNetworkCapabilities(it)?.let { capabilities ->
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        return true
                    }
                }
            }
            return false
        } else {
            with(context.connectivityManager.activeNetworkInfo) {
                this?.type == ConnectivityManager.TYPE_WIFI && isConnected
            }
        }
    }

    @Suppress("DEPRECATION")
    fun checkCellularState(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.connectivityManager.allNetworks.forEach {
                context.connectivityManager.getNetworkCapabilities(it)?.let { capabilities ->
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        return true
                    }
                }
            }
            return false
        } else {
            with(context.connectivityManager.activeNetworkInfo) {
                this?.type == ConnectivityManager.TYPE_MOBILE && isConnected
            }
        }
    }

    val Context.connectivityManager
        get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}