package com.example.networkchangesample.utils.validation

import android.content.Context
import android.net.NetworkCapabilities
import android.os.Build
import com.example.networkchangesample.network.receiver.NetworkService
import com.example.networkchangesample.utils.NetworkUtils.connectivityManager

class WifiNetworkHandler : BaseNetworkHandler() {

    override val currentValue: NetworkService.NetworkClass = NetworkService.NetworkClass.WiFiEnabled

    @Suppress("DEPRECATION")
    override fun canHandle(data: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            data.connectivityManager.allNetworks.forEach {
                data.connectivityManager.getNetworkCapabilities(it)?.let { capabilities ->
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        return true
                    }
                }
            }
            return false
        } else {
            with(data.connectivityManager.activeNetworkInfo) {
                this?.type == android.net.ConnectivityManager.TYPE_WIFI && isConnected
            }
        }
    }
}