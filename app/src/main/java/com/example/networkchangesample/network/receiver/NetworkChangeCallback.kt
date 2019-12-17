package com.example.networkchangesample.network.receiver

import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Handler
import android.os.Message
import android.os.Messenger
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkChangeCallback(private val messenger: Messenger) :
    ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        messenger.send(Message.obtain(null, NetworkService.MSG_SET_VALUE, true))
    }

    override fun onLost(network: Network) {
        //TODO: looks like android issue, when we trying to check network for cellular when it's lost it's actually active.
        Handler().postDelayed({
            messenger.send(Message.obtain(null, NetworkService.MSG_SET_VALUE, false))
        }, 500)
    }
}