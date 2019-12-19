package com.example.networkchangesample.network.receiver


import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.os.Messenger
import androidx.annotation.RequiresApi
import com.example.networkchangesample.utils.NetworkUtils.connectivityManager

class NetworkService : Service() {

    companion object {
        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_SET_VALUE = 3
        const val MSG_REQUEST = 4
    }

    private val messenger = Messenger(ServiceMessagesHandler(this))
    private val networkReceiver = NetworkChangeReceiver(messenger)
    private lateinit var networkCallback: NetworkChangeCallback

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
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } else {
                unregisterReceiver(networkReceiver)
            }
        } catch (e: Exception) {
            /**NOP*/
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun registerCallBack() {
        networkCallback = NetworkChangeCallback(messenger)
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallback
        )
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
                when {
                    networkClass == NoNetwork -> OtherEnabled
                    networkClass.ordinal % 2 == 0 -> values()[networkClass.ordinal + 1]
                    else -> values()[networkClass.ordinal - 1]
                }
        }
    }
}
