package com.example.networkchangesample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import com.example.networkchangesample.network.receiver.InternetStateChangeListener
import com.example.networkchangesample.network.receiver.NetworkService
import java.lang.ref.WeakReference

abstract class BaseActivity : AppCompatActivity() {

    private var networkListeners: MutableList<InternetStateChangeListener> = mutableListOf()

    private var mService: Messenger? = null
    private val mMessenger: Messenger by lazy { Messenger(IncomingHandler(this)) }

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
        sendSingleMessageToService()
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

    private fun internetConnectionEnabled(networkState: NetworkService.NetworkClass) {
        networkListeners.forEach { it.onInternetEnabled(networkState) }
    }

    private fun internetConnectionDisabled(networkState: NetworkService.NetworkClass) {
        networkListeners.forEach { it.onInternetDisabled(networkState) }
    }

    private fun sendSingleMessageToService() {
        val message = Message.obtain(null, NetworkService.MSG_REQUEST, this.hashCode(), 0)
        message.replyTo = mMessenger
        mService?.send(message)
    }

    class IncomingHandler(activity: BaseActivity) : Handler() {

        private val activityReference: WeakReference<BaseActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            activityReference.get()?.let { activity ->
                when (msg.what) {
                    NetworkService.MSG_SET_VALUE -> {
                        val networkState = getMessageValue(msg)
                        if (NetworkService.NetworkClass.isEnabledState(networkState))
                            activity.internetConnectionEnabled(networkState)
                        else activity.internetConnectionDisabled(networkState)
                    }
                    else -> super.handleMessage(msg)
                }
            }
        }

        private fun getMessageValue(msg: Message): NetworkService.NetworkClass =
            msg.obj?.let { it as? NetworkService.NetworkClass }
                ?: NetworkService.NetworkClass.NoNetwork
    }
}
