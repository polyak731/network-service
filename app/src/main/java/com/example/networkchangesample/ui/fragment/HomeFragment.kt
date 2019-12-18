package com.example.networkchangesample.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.networkchangesample.R
import com.example.networkchangesample.network.receiver.NetworkService

class HomeFragment : BaseNetworkFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onInternetEnabled(networkClass: NetworkService.NetworkClass) =
        handleNetworkState(networkClass)

    override fun onInternetDisabled(networkClass: NetworkService.NetworkClass) =
        handleNetworkState(networkClass)

    private fun handleNetworkState(networkClass: NetworkService.NetworkClass) {
        view?.findViewById<TextView>(R.id.text)?.text = getString(
            when (networkClass) {
                NetworkService.NetworkClass.WiFiEnabled -> R.string.wifi_network_available
                NetworkService.NetworkClass.WiFiDisabled -> R.string.wifi_network_unavailable
                NetworkService.NetworkClass.OtherEnabled -> R.string.network_available
                NetworkService.NetworkClass.OtherDisabled -> R.string.network_unavailable
                NetworkService.NetworkClass.CellularEnabled -> R.string.cellular_network_available
                NetworkService.NetworkClass.CellularDisabled -> R.string.cellular_network_unavailable
                NetworkService.NetworkClass.NoNetwork -> R.string.network_unavailable
            }
        )
    }
}