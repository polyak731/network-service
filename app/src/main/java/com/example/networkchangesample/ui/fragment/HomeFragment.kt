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
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onInternetEnable() {
        view?.findViewById<TextView>(R.id.text)?.text = getString(R.string.network_available)
    }

    override fun onInternetDisable() {
        view?.findViewById<TextView>(R.id.text)?.text = getString(R.string.network_unavailable)
    }
}