package com.psachdev.androidwebrtcclient

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

class WebrtcClientSdpObserver : SdpObserver {
    private val TAG = WebrtcClientSdpObserver::class.qualifiedName
    override fun onCreateSuccess(p0: SessionDescription?) {
        info(message = "onCreateSuccess", tag = TAG)
    }

    override fun onSetSuccess() {
        info(message = "onSetSuccess", tag = TAG)
    }

    override fun onCreateFailure(p0: String?) {
        info(message = "onCreateFailure", tag = TAG)
    }

    override fun onSetFailure(p0: String?) {
        info(message = "onSetFailure", tag = TAG)
    }
}