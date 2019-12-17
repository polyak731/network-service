package com.example.networkchangesample.ui.fragment

import androidx.fragment.app.Fragment
import com.example.networkchangesample.BaseActivity
import com.example.networkchangesample.network.receiver.InternetStateChangeListener

abstract class BaseNetworkFragment : Fragment(), InternetStateChangeListener {

    override fun onResume() {
        super.onResume()
        (requireActivity() as BaseActivity).registerNetworkStateListener(this)
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as BaseActivity).unregisterNetworkStateListener(this)
    }

    override fun onInternetEnabled() {
    }

    override fun onInternetDisabled() {
    }

    override fun onWifiEnabled() {
    }

    override fun onWifiDisabled() {
    }

    override fun onCellularEnabled() {
    }

    override fun onCellularDisabled() {
    }
}