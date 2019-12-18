package com.example.networkchangesample.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkUtils {

    @Suppress("DEPRECATION")
    fun checkNetworkState(context: Context): Boolean {
        val connectionManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectionManager.allNetworks.forEach {
                if (connectionManager.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    || connectionManager.getNetworkCapabilities(it)?.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR
                    ) == true
                ) {
                    return connectionManager.getNetworkCapabilities(it)?.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    ) == true
                }
            }
            return false
        } else {
            val networkInfo = connectionManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    @Suppress("DEPRECATION")
    fun checkWifiState(context: Context): Boolean {
        val connectionManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectionManager.allNetworks.forEach {
                if (connectionManager.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    && connectionManager.getNetworkCapabilities(it)?.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    ) == true
                ) {
                    return true
                }
            }
            return false
        } else {
            val networkInfo = connectionManager.activeNetworkInfo
            networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
        }
    }

    @Suppress("DEPRECATION")
    fun checkCellularState(context: Context): Boolean {
        val connectionManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectionManager.allNetworks.forEach {
                if (connectionManager.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
                    && connectionManager.getNetworkCapabilities(it)?.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    ) == true
                ) {
                    return true
                }
            }
            return false
        } else {
            val networkInfo = connectionManager.activeNetworkInfo
            networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected
        }
    }
}