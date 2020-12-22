package com.psachdev.androidwebrtcclient

import org.webrtc.CameraVideoCapturer

class WebrtcClientCameraEventHandler : CameraVideoCapturer.CameraEventsHandler {
    private val TAG = WebrtcClientSdpObserver::class.qualifiedName

    override fun onCameraError(p0: String?) {
        info(message = "onCameraError", tag = TAG)
    }

    override fun onCameraDisconnected() {
        info(message = "onCameraDisconnected", tag = TAG)
    }

    override fun onCameraFreezed(p0: String?) {
        info(message = "onCameraFreezed", tag = TAG)
    }

    override fun onCameraOpening(p0: String?) {
        info(message = "onCameraOpening", tag = TAG)
    }

    override fun onFirstFrameAvailable() {
        info(message = "onFirstFrameAvailable", tag = TAG)
    }

    override fun onCameraClosed() {
        info(message = "onCameraClosed", tag = TAG)
    }
}