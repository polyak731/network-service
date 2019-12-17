package com.example.networkchangesample.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.networkchangesample.R

class HomeFragment : BaseNetworkFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onWifiEnabled() {
        super.onWifiEnabled()
        view?.findViewById<TextView>(R.id.text)?.text = getString(R.string.wifi_network_available)
    }

    override fun onWifiDisabled() {
        super.onWifiDisabled()
        view?.findViewById<TextView>(R.id.text)?.text = getString(R.string.wifi_network_unavailable)
    }

    override fun onCellularEnabled() {
        super.onCellularEnabled()
        view?.findViewById<TextView>(R.id.text)?.text = getString(R.string.cellular_network_available)
    }

    override fun onCellularDisabled() {
        super.onCellularDisabled()
        view?.findViewById<TextView>(R.id.text)?.text = getString(R.string.cellular_network_unavailable)
    }

    override fun onInternetEnabled() {
        super.onInternetEnabled()
        view?.findViewById<TextView>(R.id.text)?.text = getString(R.string.network_available)
    }

    override fun onInternetDisabled() {
        super.onInternetDisabled()
        view?.findViewById<TextView>(R.id.text)?.text = getString(R.string.network_unavailable)
    }
}