package com.example.networkchangesample

import android.os.Handler
import android.os.Message
import com.example.networkchangesample.network.receiver.NetworkService
import java.lang.ref.WeakReference

class ActivityMessagesHandler(activity: BaseActivity) : Handler() {

    private val activityReference: WeakReference<BaseActivity> = WeakReference(activity)

    override fun handleMessage(msg: Message) {
        activityReference.get()?.let { activity ->
            when (msg.what) {
                NetworkService.MSG_SET_VALUE -> {
                    val networkState = getMessageValue(msg)
                    if (NetworkService.NetworkClass.isEnabledState(networkState))
                        if (msg.arg2 != 0) {
                            activity.notifyListenerWithHashCodeInternetConnected(msg.arg2, networkState)
                        } else {
                            activity.internetConnectionEnabled(networkState)
                        }
                    else {
                        activity.internetConnectionDisabled(networkState)
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private fun getMessageValue(msg: Message): NetworkService.NetworkClass =
        msg.obj?.let { it as? NetworkService.NetworkClass }
            ?: NetworkService.NetworkClass.NoNetwork
}