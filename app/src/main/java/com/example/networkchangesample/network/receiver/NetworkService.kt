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
        const val MSG_REQUEST = 4
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
        private var currentNetworkClass: NetworkClass = NetworkClass.NoNetwork

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER_CLIENT -> clients.add(msg.replyTo)
                MSG_UNREGISTER_CLIENT -> clients.remove(msg.replyTo)
                MSG_REQUEST -> handleSingleMessage(msg)
                MSG_SET_VALUE -> handleNetworkMessage()
                else -> super.handleMessage(msg)
            }
        }

        private fun handleSingleMessage(msg: Message) {
            serviceReference.get()?.let { notifyClient(msg.replyTo, currentNetworkClass) }
        }

        private fun handleNetworkMessage() {
            serviceReference.get()?.let { service ->

                currentNetworkClass = when {
                    NetworkUtils.checkWifiState(service)
                            || (NetworkUtils.checkWifiState(service)
                            && NetworkUtils.checkCellularState(service)) -> {
                        if (currentNetworkClass == NetworkClass.CellularEnabled)
                            notifyClients(NetworkClass.CellularDisabled)
                        if (currentNetworkClass == NetworkClass.OtherEnabled)
                            notifyClients(NetworkClass.OtherDisabled)
                        NetworkClass.WiFiEnabled
                    }
                    NetworkUtils.checkCellularState(service) -> {
                        if (currentNetworkClass == NetworkClass.WiFiEnabled)
                            notifyClients(NetworkClass.WiFiDisabled)
                        if (currentNetworkClass == NetworkClass.OtherEnabled)
                            notifyClients(NetworkClass.OtherDisabled)
                        NetworkClass.CellularEnabled
                    }
                    NetworkUtils.checkNetworkState(service) -> {
                        if (currentNetworkClass == NetworkClass.WiFiEnabled)
                            notifyClients(NetworkClass.WiFiDisabled)
                        if (currentNetworkClass == NetworkClass.CellularEnabled)
                            notifyClients(NetworkClass.CellularDisabled)
                        NetworkClass.OtherEnabled
                    }
                    else -> {
                        if (currentNetworkClass == NetworkClass.WiFiEnabled)
                            notifyClients(NetworkClass.WiFiDisabled)
                        if (currentNetworkClass == NetworkClass.CellularEnabled)
                            notifyClients(NetworkClass.CellularDisabled)
                        NetworkClass.NoNetwork
                    }
                }
                notifyClients(currentNetworkClass)
            }
        }

        private fun notifyClients(obj: Any?) {
            for (i in clients.size - 1 downTo 0) {
                try {
                    notifyClient(clients[i], obj)
                } catch (e: RemoteException) {
                    clients.removeAt(i)
                }
            }
        }

        private fun notifyClient(messenger: Messenger, obj: Any?) {
            messenger.send(Message.obtain(null, MSG_SET_VALUE, obj))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = messenger.binder

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
    }

    enum class NetworkClass {
        CellularEnabled,
        CellularDisabled,
        WiFiEnabled,
        WiFiDisabled,
        OtherEnabled,
        OtherDisabled,
        NoNetwork;

        companion object {

            fun isEnabledState(networkClass: NetworkClass) =
                listOf(CellularEnabled, WiFiEnabled, OtherEnabled).contains(networkClass)
        }
    }
}
