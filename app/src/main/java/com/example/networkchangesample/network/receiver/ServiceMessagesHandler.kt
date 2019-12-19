package com.example.networkchangesample.network.receiver

import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import com.example.networkchangesample.utils.validation.network.BaseNetworkHandler
import com.example.networkchangesample.utils.validation.network.CellularNetworkHandler
import com.example.networkchangesample.utils.validation.network.OtherNetworkHandler
import com.example.networkchangesample.utils.validation.network.WifiNetworkHandler
import java.lang.ref.WeakReference

class ServiceMessagesHandler(service: NetworkService) : Handler() {

    private val serviceReference: WeakReference<NetworkService> = WeakReference(service)
    private var clients = arrayListOf<Messenger>()
    private var currentNetworkClass: NetworkService.NetworkClass = NetworkService.NetworkClass.NoNetwork
    private var networkHandler: BaseNetworkHandler = WifiNetworkHandler()
        .setNext(CellularNetworkHandler().setNext(OtherNetworkHandler()))

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            NetworkService.MSG_REGISTER_CLIENT -> clients.add(msg.replyTo)
            NetworkService.MSG_UNREGISTER_CLIENT -> clients.remove(msg.replyTo)
            NetworkService.MSG_REQUEST -> handleSingleMessage(msg)
            NetworkService.MSG_SET_VALUE -> handleNetworkMessage()
            else -> super.handleMessage(msg)
        }
    }

    private fun handleSingleMessage(msg: Message) {
        serviceReference.get()?.let { notifyClientWithMessage(msg.replyTo,
            Message.obtain(null, NetworkService.MSG_SET_VALUE, currentNetworkClass)
                .apply {
                    arg1 = msg.arg1
                    arg2 = msg.arg2
                }) }
    }

    private fun handleNetworkMessage() {
        serviceReference.get()?.let { service ->

            notifyClients(NetworkService.NetworkClass.mirror(currentNetworkClass))
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
        messenger.send(Message.obtain(null, NetworkService.MSG_SET_VALUE, obj))
    }

    private fun notifyClientWithMessage(messenger: Messenger, message: Message) {
        messenger.send(message)
    }
}