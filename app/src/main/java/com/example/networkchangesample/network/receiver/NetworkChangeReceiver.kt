package com.example.networkchangesample.network.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Message
import android.os.Messenger
import com.example.networkchangesample.network.receiver.NetworkService

class NetworkChangeReceiver(private val messenger: Messenger) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        messenger.send(Message.obtain(null, NetworkService.MSG_SET_VALUE, checkInternet(context)))
    }

    private fun checkInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}