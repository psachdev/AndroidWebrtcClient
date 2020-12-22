package com.psachdev.androidwebrtcclient

import org.webrtc.*

open class WebrtcPeerConnectionObserver(private val externalTag: String): PeerConnection.Observer {
    private val TAG = WebrtcClientSdpObserver::class.qualifiedName.plus(externalTag)

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        info(message = "onSignalingChange", tag = TAG)
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        info(message = "onIceConnectionChange", tag = TAG)
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        info(message = "onIceConnectionReceivingChange", tag = TAG)
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        info(message = "onIceGatheringChange", tag = TAG)
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        info(message = "onIceCandidate", tag = TAG)
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        info(message = "onIceCandidatesRemoved", tag = TAG)
    }

    override fun onAddStream(p0: MediaStream?) {
        info(message = "onAddStream", tag = TAG)
    }

    override fun onRemoveStream(p0: MediaStream?) {
        info(message = "onRemoveStream", tag = TAG)
    }

    override fun onDataChannel(p0: DataChannel?) {
        info(message = "onDataChannel", tag = TAG)
    }

    override fun onRenegotiationNeeded() {
        info(message = "onRenegotiationNeeded", tag = TAG)
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        info(message = "onAddTrack", tag = TAG)
    }
}