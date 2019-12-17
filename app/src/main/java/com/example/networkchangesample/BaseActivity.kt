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

    var mService: Messenger? = null
    val mMessenger: Messenger by lazy { Messenger(IncomingHandler()) }
    var networkListeners: MutableList<InternetStateChangeListener> = mutableListOf()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = Messenger(service)
            try {
                var msg = Message.obtain(null, NetworkService.MSG_REGISTER_CLIENT)
                msg.replyTo = mMessenger
                mService?.send(msg)
                msg = Message.obtain(null, NetworkService.MSG_SET_VALUE, this.hashCode(), 0)
                mService?.send(msg)
            } catch (e: RemoteException) {
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
        }
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

    protected open fun internetIsUnAvailable() {
        networkListeners.forEach { it.onInternetDisable() }
    }

    protected open fun internetIsAvailable() {
        networkListeners.forEach { it.onInternetEnable() }
    }

    @SuppressLint("HandlerLeak")
    internal inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                NetworkService.MSG_SET_VALUE -> msg.obj?.let { it as? Boolean }?.let {
                    if (it) internetIsAvailable()
                    else internetIsUnAvailable()
                }
                else -> super.handleMessage(msg)
            }
        }
    }
}
