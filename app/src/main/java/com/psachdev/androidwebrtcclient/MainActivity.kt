package com.psachdev.androidwebrtcclient

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.webrtc.*
import org.webrtc.PeerConnection.IceServer


class MainActivity : AppCompatActivity() {
    private val CAMERAREQUESTPERMISSIONCODE = 101
    private val AUDIOTRACKID = "101"
    private val VIDEOTRACKID = "102"
    private lateinit var videoCapturerAndroid: VideoCapturer
    private lateinit var rootEglBase: EglBase

    private var sdpConstraints: MediaConstraints? = null
    private var localPeer: PeerConnection? = null
    private var remotePeer:PeerConnection? = null
    private var localRenderer: VideoRenderer? = null
    private var remoteRenderer: VideoRenderer? = null

    private val cameraEventhandler : CameraVideoCapturer.CameraEventsHandler = WebrtcClientCameraEventHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestCameraPermission()
    }

    override fun onResume() {
        super.onResume()
        if(::videoCapturerAndroid.isInitialized){
            videoCapturerAndroid.startCapture(1000, 1000, 30)
        }
    }

    override fun onPause() {
        super.onPause()
        if(::videoCapturerAndroid.isInitialized){
            videoCapturerAndroid.stopCapture()
        }
        if(::rootEglBase.isInitialized){
            rootEglBase.releaseSurface()
            rootEglBase.release()
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERAREQUESTPERMISSIONCODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    val videoCaptureResults = startVideoCapture()
                    setStartClickListener()
                    setCallClickListener(videoCaptureResults)
                    setHangupListener()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.camera_permission_required),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
            else -> {
                Toast.makeText(
                    this,
                    getString(R.string.invalid_permission_callback),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun requestCameraPermission(){
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                CAMERAREQUESTPERMISSIONCODE
            ); }
        else{
            val videoCaptureResults = startVideoCapture()
            setStartClickListener()
            setCallClickListener(videoCaptureResults)
            setHangupListener()
        }
    }

    private fun setHangupListener(){
        hangupButton.setOnClickListener {
            localPeer?.close()
            remotePeer?.close()
            localPeer = null
            remotePeer = null
            startButton.isEnabled = true
            callButton.isEnabled = false
            hangupButton.isEnabled = false
        }
    }
    private fun setCallClickListener(videoCaptureResults: VideoCaptureResults){
        callButton.setOnClickListener {
            startButton.isEnabled = false
            callButton.isEnabled = false
            hangupButton.isEnabled = true

            val iceServers: List<IceServer> = ArrayList()
            sdpConstraints = MediaConstraints()
            sdpConstraints!!.mandatory?.add(
                MediaConstraints.KeyValuePair(
                    "offerToReceiveAudio",
                    "true"
                )
            )
            sdpConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(
                    "offerToReceiveVideo",
                    "true"
                )
            )

            localPeer = videoCaptureResults.peerConnectionFactory.createPeerConnection(
                iceServers,
                sdpConstraints,
                object : WebrtcPeerConnectionObserver("localPeerCreation") {
                    override fun onIceCandidate(p0: IceCandidate?) {
                        super.onIceCandidate(p0)
                        onIceCandidateReceived(localPeer!!, p0)
                    }
                })

            remotePeer = videoCaptureResults.peerConnectionFactory.createPeerConnection(
                iceServers,
                sdpConstraints,
                object : WebrtcPeerConnectionObserver("remotePeerCreation") {
                    override fun onIceCandidate(p0: IceCandidate?) {
                        super.onIceCandidate(p0)
                        onIceCandidateReceived(remotePeer!!, p0)
                    }

                    override fun onAddStream(p0: MediaStream?) {
                        super.onAddStream(p0)
                        gotRemoteStream(p0!!)
                    }
                })

            val stream = videoCaptureResults.peerConnectionFactory.createLocalMediaStream("102")
            stream.addTrack(videoCaptureResults.localAudioTrack)
            stream.addTrack(videoCaptureResults.localVideoTrack)
            localPeer!!.addStream(stream)

            localPeer!!.createOffer(object : WebrtcClientSdpObserver("localCreateOffer") {
                override fun onCreateSuccess(p0: SessionDescription?) {
                    super.onCreateSuccess(p0)
                    localPeer!!.setLocalDescription(
                        WebrtcClientSdpObserver("localSetLocalDesc"),
                        p0
                    )
                    remotePeer!!.setRemoteDescription(
                        WebrtcClientSdpObserver("remoteSetRemoteDesc"),
                        p0
                    )
                    remotePeer!!.createAnswer(object : WebrtcClientSdpObserver("remoteCreateOffer") {
                        override fun onCreateSuccess(p0: SessionDescription?) {
                            super.onCreateSuccess(p0)
                            remotePeer!!.setLocalDescription(
                                WebrtcClientSdpObserver("remoteSetLocalDesc"),
                                p0
                            )
                            localPeer!!.setRemoteDescription(
                                WebrtcClientSdpObserver("localSetRemoteDesc"),
                                p0
                            )
                        }
                    }, MediaConstraints())
                }
            }, sdpConstraints)
        }
    }

    private fun setStartClickListener(){
        startButton.setOnClickListener {
            startButton.isEnabled = false
            callButton.isEnabled = true
            localVideoView.visibility = View.VISIBLE
        }
    }

    private data class VideoCaptureResults(
        val peerConnectionFactory: PeerConnectionFactory,
        val localAudioTrack: AudioTrack,
        val localVideoTrack: VideoTrack
    )
    private fun startVideoCapture(): VideoCaptureResults{
        videoCapturerAndroid = createVideoCapturer(cameraEventhandler)!!
        val peerConnectionFactory = initializePeerConnectionFactory()
        val localAudioTrack = getLocalAudioTrack(peerConnectionFactory)
        val localVideoTrack = getLocalVideoTrack(peerConnectionFactory, videoCapturerAndroid)
        videoCapturerAndroid.startCapture(1000, 1000, 30)
        initializeVideoView()
        localVideoTrack.addRenderer(VideoRenderer(localVideoView))
        return VideoCaptureResults(peerConnectionFactory, localAudioTrack, localVideoTrack)
    }

    private fun initializeVideoView(){
        rootEglBase = EglBase.create()
        localVideoView.init(rootEglBase.eglBaseContext, null)
        //localVideoView.setMirror(true)
        localVideoView.setZOrderMediaOverlay(true)

        remoteVideoView.init(rootEglBase.eglBaseContext, null)
        //localVideoView.setMirror(true)
        remoteVideoView.setZOrderMediaOverlay(true)

    }

    private fun initializePeerConnectionFactory(): PeerConnectionFactory{
        PeerConnectionFactory.initializeAndroidGlobals(this, true)
        val options = PeerConnectionFactory.Options()
        options.disableEncryption = false
        options.disableNetworkMonitor = false
        return PeerConnectionFactory(options)
    }

    private fun getLocalAudioTrack(peerConnectionFactory: PeerConnectionFactory): AudioTrack{
        val constraints = MediaConstraints()
        val audioSource = peerConnectionFactory.createAudioSource(constraints)
        return peerConnectionFactory.createAudioTrack(AUDIOTRACKID, audioSource)
    }

    private fun getLocalVideoTrack(
        peerConnectionFactory: PeerConnectionFactory,
        videoCapturerAndroid: VideoCapturer
    ): VideoTrack{
        val videoSource: VideoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid)
       return peerConnectionFactory.createVideoTrack(VIDEOTRACKID, videoSource)
    }

    private fun createVideoCapturer(cameraEventHandler: CameraVideoCapturer.CameraEventsHandler): VideoCapturer? {
        return createCameraCapturer(Camera1Enumerator(false), cameraEventHandler)
    }

    private fun createCameraCapturer(
        enumerator: CameraEnumerator,
        cameraEventHandler: CameraVideoCapturer.CameraEventsHandler
    ): VideoCapturer? {
        val deviceNames = enumerator.deviceNames

        // Find selfie camera
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(
                    deviceName,
                    cameraEventHandler
                )
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        // Not able to find a front camera.
        // Look for other cameras
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(
                    deviceName,
                    cameraEventHandler
                )
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    private fun onIceCandidateReceived(peer: PeerConnection, iceCandidate: IceCandidate?) {
        if (peer === localPeer) {
            remotePeer!!.addIceCandidate(iceCandidate)
        } else {
            localPeer!!.addIceCandidate(iceCandidate)
        }
    }

    private fun gotRemoteStream(stream: MediaStream) {
        //we have remote video stream. add to the renderer.
        val videoTrack = stream.videoTracks.first
        val audioTrack = stream.audioTracks.first
        runOnUiThread {
            try {
                remoteRenderer = VideoRenderer(remoteVideoView)
                remoteVideoView.visibility = View.VISIBLE
                videoTrack.addRenderer(remoteRenderer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}