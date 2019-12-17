package com.example.networkchangesample.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkUtils {

    fun checkNetworkState(context: Context): Boolean {
        val connectionManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var isWifiConnected = false
            var isCellularConnected = false
            connectionManager.allNetworks.forEach {
                isWifiConnected = connectionManager.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
                isCellularConnected = connectionManager.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
            }
            isCellularConnected || isWifiConnected
        } else {
            val networkInfo = connectionManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    fun checkWifiState(context: Context): Boolean {
        val connectionManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var isWifiConnected = false
            connectionManager.allNetworks.forEach {
                isWifiConnected = connectionManager.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
            }
            isWifiConnected
        } else {
            val networkInfo = connectionManager.activeNetworkInfo
            networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
        }
    }

    fun checkCellularState(context: Context): Boolean {
        val connectionManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var isCellularConnected = false
            connectionManager.allNetworks.forEach {
                isCellularConnected = connectionManager.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
            }
            isCellularConnected
        } else {
            val networkInfo = connectionManager.activeNetworkInfo
            networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected
        }
    }
}