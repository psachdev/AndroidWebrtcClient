package com.psachdev.androidwebrtcclient

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class WebrtcClientSdpObserver(private val externalTag: String) : SdpObserver {
    private val loggerTag = WebrtcClientSdpObserver::class.qualifiedName.plus(externalTag)
    override fun onCreateSuccess(p0: SessionDescription?) {
        info(message = "onCreateSuccess", tag = loggerTag)
    }

    override fun onSetSuccess() {
        info(message = "onSetSuccess", tag = loggerTag)
    }

    override fun onCreateFailure(p0: String?) {
        info(message = "onCreateFailure", tag = loggerTag)
    }

    override fun onSetFailure(p0: String?) {
        info(message = "onSetFailure", tag = loggerTag)
    }
}