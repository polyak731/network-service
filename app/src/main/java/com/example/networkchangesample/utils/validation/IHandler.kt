package com.example.networkchangesample.utils.validation

interface IHandler<Input, Output> {

    fun handle(data: Input) : Output
    fun setNext(handler: IHandler<Input, Output>) : IHandler<Input, Output>
    fun canHandle(data: Input) : Boolean
}