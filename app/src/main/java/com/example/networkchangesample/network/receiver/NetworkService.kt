package com.example.networkchangesample.network.receiver


import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.*
import androidx.annotation.RequiresApi
import com.example.networkchangesample.utils.NetworkUtils
import java.lang.ref.WeakReference

class NetworkService : Service() {

    companion object {
        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_SET_VALUE = 3
        const val MSG_DEFAULT = 4
        const val MSG_WIFI_RADIO = 5
        const val MSG_CELLULAR_RADIO = 6
        const val MSG_REQUEST = 7
    }

    private val messenger = Messenger(IncomingHandler(this))
    private val networkReceiver = NetworkChangeReceiver(messenger)
    private lateinit var networkCallback: NetworkChangeCallback

    @Suppress("DEPRECATION")
    private fun registerFilter() {
        try {
            registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        } catch (e: Exception) {
            /**NOP*/
        }
    }

    private class IncomingHandler(service: NetworkService) : Handler() {

        private val serviceReference: WeakReference<NetworkService> = WeakReference(service)
        private var clients = arrayListOf<Messenger>()
        private var currentNetworkClass: NetworkClass = NetworkClass.No

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
            serviceReference.get()?.let { service ->
                when (currentNetworkClass) {
                    NetworkClass.Other -> {
                        notifyClient(
                            msg.replyTo,
                            MSG_DEFAULT,
                            NetworkUtils.checkNetworkState(service)
                        )
                    }
                    NetworkClass.WiFi -> {
                        notifyClient(
                            msg.replyTo,
                            MSG_WIFI_RADIO,
                            NetworkUtils.checkWifiState(service)
                        )
                    }
                    NetworkClass.Cellular -> {
                        notifyClient(
                            msg.replyTo,
                            MSG_CELLULAR_RADIO,
                            NetworkUtils.checkCellularState(service)
                        )
                    }
                    NetworkClass.No -> {
                        notifyClient(
                            msg.replyTo,
                            MSG_DEFAULT,
                            NetworkUtils.checkNetworkState(service)
                        )
                    }
                }
            }
        }

        private fun handleNetworkMessage(msg: Message) {
            println("handleNetworkMessage")
            serviceReference.get()?.let { service ->

                currentNetworkClass = when {
                    NetworkUtils.checkWifiState(service)
                            || (NetworkUtils.checkWifiState(service) && NetworkUtils.checkCellularState(service))-> {
                        notifyClients(MSG_WIFI_RADIO, true)
                        NetworkClass.WiFi
                    }
                    NetworkUtils.checkCellularState(service) -> {
                        notifyClients(MSG_CELLULAR_RADIO, true)
                        NetworkClass.Cellular
                    }
                    NetworkUtils.checkNetworkState(service) -> {
                        notifyClients(MSG_DEFAULT, true)
                        NetworkClass.Other
                    }
                    else -> {
                        notifyClients(MSG_DEFAULT, msg.obj)
                        NetworkClass.No
                    }
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
        networkCallback = NetworkChangeCallback(messenger)
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
