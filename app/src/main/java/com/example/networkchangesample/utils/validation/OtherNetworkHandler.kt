package com.example.networkchangesample.utils.validation

import android.content.Context
import android.net.NetworkCapabilities
import android.os.Build
import com.example.networkchangesample.network.receiver.NetworkService
import com.example.networkchangesample.utils.NetworkUtils.connectivityManager

class OtherNetworkHandler : BaseNetworkHandler() {

    override val currentValue: NetworkService.NetworkClass = NetworkService.NetworkClass.OtherEnabled

    @Suppress("DEPRECATION")
    override fun canHandle(data: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            data.connectivityManager.allNetworks.forEach {
                data.connectivityManager.getNetworkCapabilities(it)?.let { capabilities ->
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    }
                }
            }
            return false
        } else {
            data.connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }
}