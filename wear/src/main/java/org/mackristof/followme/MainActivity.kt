package org.mackristof.followme

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.BoxInsetLayout
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.wearable.Wearable

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : WearableActivity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private val TAG: String = MainActivity.javaClass.simpleName
    private var mContainerView: BoxInsetLayout? = null
    private var mTextView: TextView? = null
    private var mClockView: TextView? = null
    private var mGoogleApiClient : GoogleApiClient? = null

    private val GPS_UPDATE_INTERVAL_MS: Long = 60000
    private val GPS_FASTEST_INTERVAL_MS: Long = 30000
    private var curentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setAmbientEnabled()
        if (hasGPSPermission()) {
            askGPS()
        } else {
            Log.e(TAG, "not enough permission for application with GPS location")
        }

        mContainerView = findViewById(R.id.container) as BoxInsetLayout
        mTextView = findViewById(R.id.text) as TextView
        mClockView = findViewById(R.id.clock) as TextView
    }
    override fun onStop(){
        super.onStop()
        if(mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.disconnect()
        }
    }

    override fun onPause(){
        super.onPause()
    }

    override fun onResume(){
        super.onResume()
        if(!mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.connect()
        }
    }

    private fun askGPS() {
        //start connect API
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        mGoogleApiClient?.connect()
    }


    // callback google api client
    override fun onConnected(bundle: Bundle?) {
        updateDisplay("connected, wait for location ...")

        if (hasGPSPermission()){
            val locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(GPS_UPDATE_INTERVAL_MS)
                    .setFastestInterval(GPS_FASTEST_INTERVAL_MS)

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                    .setResultCallback(ResultCallback<com.google.android.gms.common.api.Status> { status ->
                        if (status.status.isSuccess) {
                            if (Log.isLoggable(TAG, Log.DEBUG)) {
                                Log.d(TAG, "Successfully requested location updates")
                            }
                        } else {
                            Log.e(TAG,
                                    "Failed in requesting location updates, " + "status code: " + status.statusCode
                                            + ", message: " + status.statusMessage)
                        }
                    })
        }
    }

    private fun hasGPSPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }


    // google client api connection suspended
    override fun onConnectionSuspended(i: Int) {
        Log.d(TAG, "onConnectionSuspended(): connection to location client suspended")
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
    }

    // google client api connection failed
    override fun onConnectionFailed(connectionResult: ConnectionResult?) {
        Log.e(TAG, "onConnectionFailed(): " + connectionResult?.errorMessage)
    }

    // location change listener
    override fun onLocationChanged(location: Location?) {
        curentLocation = location
        updateDisplay("location: "+location?.latitude + " , "+ location?.longitude)
        Log.i(TAG,"location changed: ("+ location?.latitude+" , "+location?.longitude+")")
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
        updateDisplay("ambient")
    }

    override fun onUpdateAmbient() {
        super.onUpdateAmbient()
        updateDisplay("update ambient")
    }

    override fun onExitAmbient() {
        updateDisplay("exitAmbient")
        super.onExitAmbient()
    }

    private fun updateDisplay(text: String) {
        mTextView?.text = text
        if (isAmbient) {

            mContainerView!!.setBackgroundColor(resources.getColor(android.R.color.black))
            mTextView!!.setTextColor(resources.getColor(android.R.color.white))
            mClockView!!.visibility = View.VISIBLE

            mClockView!!.text = AMBIENT_DATE_FORMAT.format(Date())
        } else {
            mContainerView!!.background = null
            mTextView!!.setTextColor(resources.getColor(android.R.color.black))
            mClockView!!.visibility = View.GONE
        }
    }

    companion object {
        private val AMBIENT_DATE_FORMAT = SimpleDateFormat("HH:mm", Locale.US)
    }
}
