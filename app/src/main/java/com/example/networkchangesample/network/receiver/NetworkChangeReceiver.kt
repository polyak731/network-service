package com.example.networkchangesample.network.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Message
import android.os.Messenger
import com.example.networkchangesample.utils.NetworkUtils.checkNetworkState

class NetworkChangeReceiver(private val messenger: Messenger) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        messenger.send(Message.obtain(null, NetworkService.MSG_SET_VALUE, checkNetworkState(context)))
    }
}