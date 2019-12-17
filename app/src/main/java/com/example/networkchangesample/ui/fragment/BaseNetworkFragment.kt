package com.example.networkchangesample.ui.fragment

import androidx.fragment.app.Fragment
import com.example.networkchangesample.BaseActivity
import com.example.networkchangesample.network.receiver.InternetStateChangeListener

abstract class BaseNetworkFragment : Fragment(), InternetStateChangeListener {

    override fun onResume() {
        super.onResume()
        (requireActivity() as BaseActivity).networkListeners.add(this)
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as BaseActivity).networkListeners.remove(this)
    }

    override fun onInternetEnable() {
    }

    override fun onInternetDisable() {
    }
}