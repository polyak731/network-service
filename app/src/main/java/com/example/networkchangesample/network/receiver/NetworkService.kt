package com.example.networkchangesample.network.receiver


import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.*
import androidx.annotation.RequiresApi
import com.example.networkchangesample.utils.NetworkUtils

class NetworkService : Service() {

    companion object {
        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_SET_VALUE = 3
        const val MSG_WIFI_RADIO = 4
        const val MSG_CELLULAR_RADIO = 5
    }

    private val messenger = Messenger(IncomingHandler())
    private var clients = arrayListOf<Messenger>()
    private val networkReceiver = NetworkChangeReceiver(messenger)
    private var currentNetworkClass: NetworkClass = NetworkClass.No

    @Suppress("DEPRECATION")
    private fun registerFilter() {
        try {
            registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        } catch (e: Exception) {
            /**NOP*/
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            messenger.send(Message.obtain(null, MSG_SET_VALUE, true))
        }

        override fun onLost(network: Network) {
            messenger.send(Message.obtain(null, MSG_SET_VALUE, false))
        }
    }

    @SuppressLint("HandlerLeak")
    internal inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER_CLIENT -> clients.add(msg.replyTo)
                MSG_UNREGISTER_CLIENT -> clients.remove(msg.replyTo)
                MSG_SET_VALUE ->
                    currentNetworkClass = when {
                        NetworkUtils.checkWifiState(this@NetworkService) -> {
                            notifyClients(MSG_WIFI_RADIO, msg.obj)
                            NetworkClass.WiFi
                        }
                        NetworkUtils.checkCellularState(this@NetworkService) -> {
                            notifyClients(MSG_CELLULAR_RADIO, msg.obj)
                            NetworkClass.Cellular
                        }
                        NetworkUtils.checkNetworkState(this@NetworkService) -> {
                            notifyClients(MSG_SET_VALUE, msg.obj)
                            NetworkClass.Other
                        }
                        else -> {
                            notifyClients(MSG_SET_VALUE, msg.obj)
                            NetworkClass.No
                        }
                    }
                else -> super.handleMessage(msg)
            }
        }

        private fun notifyClients(what: Int, obj: Any?) {
            for (i in clients.size - 1 downTo 0) {
                val message = Message.obtain(null, what, obj)
                try {
                    clients[i].send(message)
                } catch (e: RemoteException) {
                    clients.removeAt(i)
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = messenger.binder

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            registerCallBack()
        } else {
            registerFilter()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                (getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
                    ?.unregisterNetworkCallback(networkCallback)
            } catch (e: Exception) {
                /**NOP*/
            }
        } else {
            try {
                unregisterReceiver(networkReceiver)
            } catch (ignored: Exception) {
                /**NOP*/
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun registerCallBack() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        connectivityManager.registerNetworkCallback(
            builder.build(),
            networkCallback
        )
    }

    private enum class NetworkClass {
        Cellular,
        WiFi,
        Other,
        No
    }
}
