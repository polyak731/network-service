package com.example.networkchangesample.network.receiver

import android.net.ConnectivityManager
import android.net.Network
import android.os.*
import androidx.annotation.AnyThread
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkChangeCallback(private val messenger: Messenger) :
    ConnectivityManager.NetworkCallback() {

    private var action: Runnable = Runnable {
        messenger.send(Message.obtain(null, NetworkService.MSG_SET_VALUE, false))
    }

    private var mainHandler: Handler = Handler(Looper.getMainLooper())

    @AnyThread
    override fun onAvailable(network: Network) {
        mainHandler.removeCallbacks(action)
        messenger.send(Message.obtain(null, NetworkService.MSG_SET_VALUE, true))
    }

    @AnyThread
    override fun onLost(network: Network) {
        //TODO: looks like android issue, when we trying to check network for cellular when it's lost it's actually active.
        mainHandler.removeCallbacks(action)
        mainHandler.postDelayed(action, 500)
    }
}