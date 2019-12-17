package com.example.networkchangesample.network.receiver

interface InternetStateChangeListener {
    fun onInternetEnabled()
    fun onInternetDisabled()
    fun onWifiEnabled()
    fun onWifiDisabled()
    fun onCellularEnabled()
    fun onCellularDisabled()
}