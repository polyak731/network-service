package com.example.networkchangesample.network.receiver

interface InternetStateChangeListener {
    fun onInternetEnabled(networkClass: NetworkService.NetworkClass)
    fun onInternetDisabled(networkClass: NetworkService.NetworkClass)
}