package com.example.networkchangesample.utils.validation.network

import android.content.Context
import com.example.networkchangesample.network.receiver.NetworkService
import com.example.networkchangesample.utils.NetworkUtils.checkNetworkState
import com.example.networkchangesample.utils.validation.network.BaseNetworkHandler

class OtherNetworkHandler : BaseNetworkHandler() {

    override val currentValue: NetworkService.NetworkClass = NetworkService.NetworkClass.OtherEnabled

    @Suppress("DEPRECATION")
    override fun canHandle(data: Context): Boolean {
        return checkNetworkState(data)
    }
}