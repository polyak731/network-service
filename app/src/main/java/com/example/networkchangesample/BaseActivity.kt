package com.example.networkchangesample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.appcompat.app.AppCompatActivity
import com.example.networkchangesample.network.receiver.InternetStateChangeListener
import com.example.networkchangesample.network.receiver.NetworkService

abstract class BaseActivity : AppCompatActivity() {

    private var networkListeners: MutableList<InternetStateChangeListener> = mutableListOf()

    private var mService: Messenger? = null
    private val mMessenger: Messenger by lazy { Messenger(ActivityMessagesHandler(this)) }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = Messenger(service)
            try {
                val msg = Message.obtain(null, NetworkService.MSG_REGISTER_CLIENT)
                msg.replyTo = mMessenger
                mService?.send(msg)
                sendSingleMessageToService()
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
        sendSingleMessageForSingleSubscriber(listener.hashCode())
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

    fun internetConnectionEnabled(networkState: NetworkService.NetworkClass) {
        networkListeners.forEach { it.onInternetEnabled(networkState) }
    }

    fun internetConnectionDisabled(networkState: NetworkService.NetworkClass) {
        networkListeners.forEach { it.onInternetDisabled(networkState) }
    }

    fun notifyListenerWithHashCodeInternetConnected(arg: Int, networkState: NetworkService.NetworkClass) {
        networkListeners.firstOrNull { it.hashCode() == arg }?.onInternetEnabled(networkState)
    }

    fun sendSingleMessageToService() {
        val message = Message
            .obtain(null, NetworkService.MSG_REQUEST)
            .apply { replyTo = mMessenger }
        mService?.send(message)
    }

    private fun sendSingleMessageForSingleSubscriber(arg: Int) {
        val message = Message.obtain(null, NetworkService.MSG_REQUEST).apply {
            replyTo = mMessenger
            arg2 = arg
        }
        mService?.send(message)
    }
}
