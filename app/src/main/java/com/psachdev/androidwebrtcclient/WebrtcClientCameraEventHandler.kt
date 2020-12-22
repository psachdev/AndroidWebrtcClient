package com.psachdev.androidwebrtcclient

import org.webrtc.CameraVideoCapturer

class WebrtcClientCameraEventHandler : CameraVideoCapturer.CameraEventsHandler {
    private val loggerTag = WebrtcClientSdpObserver::class.qualifiedName

    override fun onCameraError(p0: String?) {
        info(message = "onCameraError", tag = loggerTag)
    }

    override fun onCameraDisconnected() {
        info(message = "onCameraDisconnected", tag = loggerTag)
    }

    override fun onCameraFreezed(p0: String?) {
        info(message = "onCameraFreezed", tag = loggerTag)
    }

    override fun onCameraOpening(p0: String?) {
        info(message = "onCameraOpening", tag = loggerTag)
    }

    override fun onFirstFrameAvailable() {
        info(message = "onFirstFrameAvailable", tag = loggerTag)
    }

    override fun onCameraClosed() {
        info(message = "onCameraClosed", tag = loggerTag)
    }
}