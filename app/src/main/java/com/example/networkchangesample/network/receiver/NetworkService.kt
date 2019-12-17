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

        const val MSG_REQUEST = 6
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
                MSG_REQUEST -> handleSingleMessage(msg)
                MSG_SET_VALUE -> handleNetworkMessage(msg)
                else -> super.handleMessage(msg)
            }
        }

        private fun handleSingleMessage(msg: Message) {
            when (currentNetworkClass) {
                NetworkClass.Other -> {
                    notifyClient(msg.replyTo, MSG_SET_VALUE, NetworkUtils.checkNetworkState(this@NetworkService))
                }
                NetworkClass.WiFi -> {
                    notifyClient(msg.replyTo, MSG_WIFI_RADIO, NetworkUtils.checkWifiState(this@NetworkService))
                }
                NetworkClass.Cellular -> {
                    notifyClient(msg.replyTo, MSG_CELLULAR_RADIO, NetworkUtils.checkCellularState(this@NetworkService))
                }
                NetworkClass.No -> {
                    notifyClient(msg.replyTo, MSG_SET_VALUE, NetworkUtils.checkNetworkState(this@NetworkService))
                }
            }
        }

        private fun handleNetworkMessage(msg: Message) {
            currentNetworkClass = when {
                NetworkUtils.checkWifiState(this@NetworkService) -> {
                    if (currentNetworkClass == NetworkClass.Cellular) notifyClients(MSG_CELLULAR_RADIO, false)
                    notifyClients(MSG_WIFI_RADIO, msg.obj)
                    NetworkClass.WiFi
                }
                NetworkUtils.checkCellularState(this@NetworkService) -> {
                    if (currentNetworkClass == NetworkClass.WiFi) notifyClients(MSG_WIFI_RADIO, false)
                    notifyClients(MSG_CELLULAR_RADIO, msg.obj)
                    NetworkClass.Cellular
                }
                NetworkUtils.checkNetworkState(this@NetworkService) -> {
                    if (currentNetworkClass == NetworkClass.WiFi) notifyClients(MSG_WIFI_RADIO, false)
                    if (currentNetworkClass == NetworkClass.Cellular) notifyClients(MSG_CELLULAR_RADIO, false)
                    notifyClients(MSG_SET_VALUE, msg.obj)
                    NetworkClass.Other
                }
                else -> {
                    if (currentNetworkClass == NetworkClass.WiFi) notifyClients(MSG_WIFI_RADIO, false)
                    if (currentNetworkClass == NetworkClass.Cellular) notifyClients(MSG_CELLULAR_RADIO, false)
                    notifyClients(MSG_SET_VALUE, msg.obj)
                    NetworkClass.No
                }
            }
        }

        private fun notifyClients(what: Int, obj: Any?) {
            for (i in clients.size - 1 downTo 0) {
                try {
                    notifyClient(clients[i], what, obj)
                } catch (e: RemoteException) {
                    clients.removeAt(i)
                }
            }
        }

        private fun notifyClient(messenger: Messenger, what: Int, obj: Any?) {
            val message = Message.obtain(null, what, obj)
            messenger.send(message)
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
        messenger.send(Message.obtain(null, MSG_SET_VALUE, NetworkUtils.checkNetworkState(this)))
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
