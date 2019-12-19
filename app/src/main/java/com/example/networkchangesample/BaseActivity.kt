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
        sendSingleMessageForSingleSubscriber(networkListeners.indexOf(listener))
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
        if (networkListeners.lastIndex >= arg || arg >= 0) {
            networkListeners[arg].onInternetEnabled(networkState)
        }
    }

    fun notifyListenerWithHashCodeInternetDisconnected(arg: Int, networkState: NetworkService.NetworkClass) {
        if (networkListeners.lastIndex >= arg || arg >= 0) {
            networkListeners[arg].onInternetDisabled(networkState)
        }
    }

    fun sendSingleMessageToService() {
        mService?.send(Message
            .obtain(null, NetworkService.MSG_REQUEST)
            .apply { replyTo = mMessenger })
    }

    private fun sendSingleMessageForSingleSubscriber(arg: Int) {
        mService?.send(Message
            .obtain(null, NetworkService.MSG_REQUEST)
            .apply { arg2 = arg })
    }
}
