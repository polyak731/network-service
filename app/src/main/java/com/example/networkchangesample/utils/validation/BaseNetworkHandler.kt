package com.example.networkchangesample.utils.validation

import android.content.Context
import com.example.networkchangesample.network.receiver.NetworkService

abstract class BaseNetworkHandler : IHandler<Context, NetworkService.NetworkClass> {

    protected abstract val currentValue: NetworkService.NetworkClass
    private var nextHandler: IHandler<Context, NetworkService.NetworkClass>? = null

    override fun handle(data: Context): NetworkService.NetworkClass {
        if (canHandle(data)) {
            return currentValue
        }
        return nextHandler?.handle(data) ?: NetworkService.NetworkClass.NoNetwork
    }

    override fun setNext(handler: IHandler<Context, NetworkService.NetworkClass>) : BaseNetworkHandler {
        nextHandler = handler
        return this
    }
}