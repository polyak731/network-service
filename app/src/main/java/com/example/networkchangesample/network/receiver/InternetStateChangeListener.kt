package com.example.networkchangesample.network.receiver

interface InternetStateChangeListener {
    fun onInternetEnable()
    fun onInternetDisable()
}