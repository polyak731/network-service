package com.example.networkchangesample

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import com.example.networkchangesample.network.receiver.InternetStateChangeListener
import com.example.networkchangesample.network.receiver.NetworkService

abstract class BaseActivity : AppCompatActivity() {

    private var networkListeners: MutableList<InternetStateChangeListener> = mutableListOf()

    private var mService: Messenger? = null
    private val mMessenger: Messenger by lazy { Messenger(IncomingHandler()) }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = Messenger(service)
            try {
                val msg = Message.obtain(null, NetworkService.MSG_REGISTER_CLIENT)
                msg.replyTo = mMessenger
                mService?.send(msg)
            } catch (e: RemoteException) {
                /**NOP*/
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            try {
                val msg = Message.obtain(null, NetworkService.MSG_UNREGISTER_CLIENT)
                msg.replyTo = mMessenger
                mService?.send(msg)
            } catch (e: RemoteException) {
                /**NOP*/
            }
            mService = null
        }
    }

    fun registerNetworkStateListener(listener: InternetStateChangeListener) {
        networkListeners.add(listener)
        val message = Message.obtain(null, NetworkService.MSG_REQUEST, this.hashCode(), 0)
        message.replyTo = mMessenger
        mService?.send(message)
    }

    fun unregisterNetworkStateListener(listener: InternetStateChangeListener) {
        networkListeners.remove(listener)
    }

    override fun onResume() {
        super.onResume()
        bindService(
            Intent(this, NetworkService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onDestroy() {
        try {
            unbindService(serviceConnection)
        } catch (e: Exception) {
            /**NOP*/
        }
        super.onDestroy()
    }

    private fun internetIsUnAvailable() {
        networkListeners.forEach(InternetStateChangeListener::onInternetEnabled)
    }

    private fun internetIsAvailable() {
        networkListeners.forEach(InternetStateChangeListener::onInternetEnabled)
    }

    private fun wifiConnected() {
        networkListeners.forEach(InternetStateChangeListener::onWifiEnabled)
    }

    private fun wifiDisconnected() {
        networkListeners.forEach(InternetStateChangeListener::onWifiDisabled)
    }

    private fun cellularConnected() {
        networkListeners.forEach(InternetStateChangeListener::onCellularEnabled)
    }

    private fun cellularDisconnected() {
        networkListeners.forEach(InternetStateChangeListener::onCellularDisabled)
    }

    @SuppressLint("HandlerLeak")
    internal inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                NetworkService.MSG_SET_VALUE -> {
                    if (getMessageValue(msg)) internetIsAvailable()
                    else internetIsUnAvailable()
                }
                NetworkService.MSG_WIFI_RADIO -> {
                    if (getMessageValue(msg)) wifiConnected()
                    else wifiDisconnected()
                }
                NetworkService.MSG_CELLULAR_RADIO -> {
                    if (getMessageValue(msg)) cellularConnected()
                    else cellularDisconnected()
                }
                else -> super.handleMessage(msg)
            }
        }

        private fun getMessageValue(msg: Message): Boolean =
            msg.obj?.let { it as? Boolean } ?: false
    }
}
