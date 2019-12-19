package com.example.networkchangesample.network.receiver


import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.*
import androidx.annotation.RequiresApi
import com.example.networkchangesample.utils.NetworkUtils.connectivityManager
import com.example.networkchangesample.utils.validation.IHandler
import com.example.networkchangesample.utils.validation.network.BaseNetworkHandler
import com.example.networkchangesample.utils.validation.network.CellularNetworkHandler
import com.example.networkchangesample.utils.validation.network.OtherNetworkHandler
import com.example.networkchangesample.utils.validation.network.WifiNetworkHandler
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

    private class IncomingHandler(service: NetworkService) : Handler() {

        private val serviceReference: WeakReference<NetworkService> = WeakReference(service)
        private var clients = arrayListOf<Messenger>()
        private var currentNetworkClass: NetworkClass = NetworkClass.NoNetwork
        private var networkHandler: BaseNetworkHandler = WifiNetworkHandler()
            .setNext(CellularNetworkHandler().setNext(OtherNetworkHandler()))

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
            serviceReference.get()?.let { notifyClientWithMessage(msg.replyTo,
                Message.obtain(null, MSG_SET_VALUE, currentNetworkClass)
                    .apply {
                        arg1 = msg.arg1
                        arg2 = msg.arg2
                    }) }
        }

        private fun handleNetworkMessage() {
            serviceReference.get()?.let { service ->

                notifyClients(NetworkClass.mirror(currentNetworkClass))
                currentNetworkClass = networkHandler.handle(service)
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

        private fun notifyClientWithMessage(messenger: Messenger, message: Message) {
            messenger.send(message)
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
        networkCallback = NetworkChangeCallback(messenger)
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
    }

    @Suppress("DEPRECATION")
    private fun registerFilter() {
        try {
            registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        } catch (e: Exception) {
            /**NOP*/
        }
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

            fun mirror(networkClass: NetworkClass) =
                if (networkClass == NoNetwork) OtherEnabled else values()[networkClass.ordinal + 1]
        }
    }
}
