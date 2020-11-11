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


class MainActivity : AppCompatActivity() {
    private val CAMERAREQUESTPERMISSIONCODE = 101
    private val AUDIOTRACKID = "101"
    private val VIDEOTRACKID = "102"
    private lateinit var videoCapturerAndroid: VideoCapturer
    private lateinit var rootEglBase: EglBase

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



    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERAREQUESTPERMISSIONCODE -> {
                if ((grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startVideoCapture()
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                Toast.makeText(this, getString(R.string.invalid_permission_callback), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                    CAMERAREQUESTPERMISSIONCODE); }
        else{
            startVideoCapture()
        }
    }

    private fun startVideoCapture(){
        videoCapturerAndroid = createVideoCapturer()!!
        val peerConnectionFactory = initializePeerConnectionFactory()
        val localAudioTrack = getLocalAudioTrack(peerConnectionFactory)
        val localVideoTrack = getLocalVideoTrack(peerConnectionFactory, videoCapturerAndroid)
        videoCapturerAndroid.startCapture(1000, 1000, 30)
        initializeVideoView()
        localVideoTrack.addRenderer(VideoRenderer(videoView))
    }

    private fun initializeVideoView(){
        rootEglBase = EglBase.create()
        videoView.init(rootEglBase.eglBaseContext, null)
        videoView.visibility = View.VISIBLE
        videoView.setMirror(true)
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

    private fun getLocalVideoTrack(peerConnectionFactory: PeerConnectionFactory,
                                   videoCapturerAndroid: VideoCapturer): VideoTrack{
        val videoSource: VideoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid)
       return peerConnectionFactory.createVideoTrack(VIDEOTRACKID, videoSource)
    }

    private fun createVideoCapturer(): VideoCapturer? {
        return createCameraCapturer(Camera1Enumerator(false))
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames

        // Find selfie camera
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        // Not able to find a front camera.
        // Look for other cameras
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }
}