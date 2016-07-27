package org.mackristof.followme

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.GpsStatus
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.common.api.ResultCallback
//import com.google.android.gms.common.api.Status
import android.location.LocationListener
import android.location.LocationProvider
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationServices
import com.google.android.gms.wearable.Wearable


class GpsService: Service(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{



    var mGoogleApiClient: GoogleApiClient? = null
    var broadcaster: LocalBroadcastManager? = null
    var currentLocation: org.mackristof.followme.Location? = null
    var geoGrid: GeoGrid?=null
    var mlocManager: LocationManager?= null
    var gpsSatsAvailable: Int = 0
    private var gpsLocationListerner: GpsLocationListener? = null

//    val locationRequest = LocationRequest.create()
//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//            .setInterval(Constants.GPS_UPDATE_INTERVAL_MS)
//            .setFastestInterval(Constants.GPS_FASTEST_INTERVAL_MS)

    override fun onCreate(){
        Log.i(Constants.TAG,"gpsService created")
    }

    override fun onDestroy(){
        Log.i(Constants.TAG,"gpsService stopped ")
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this)
        mGoogleApiClient?.disconnect()
    }

    override fun onStartCommand(intent:Intent , flags: Int, startId: Int ):Int {
        Log.i(Constants.TAG,"gpsService started ")

        if (Utils.hasGPS(this.applicationContext)) {

            geoGrid = GeoGrid(this)
            gpsLocationListerner = GpsLocationListener()
            mlocManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            mlocManager?.addGpsStatusListener(gpsLocationListerner)
            mlocManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, gpsLocationListerner)
        } else {
            //TODO send message to start gps on other side

        }

//        mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
//                .addApiIfAvailable(Wearable.API)
//                .build()
//        mGoogleApiClient?.connect()
        broadcaster = LocalBroadcastManager.getInstance(applicationContext)
        if (intent.getBooleanExtra(Constants.INTENT_LOCATION_EXTRA_PUBLISH,false)){
            if (Utils.isRunningOnWatch(this)){
                //TODO log current loc to data API
            } else {
                //TODO start service publish on mobile

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }




    override fun onConnected(p0: Bundle?) {
        broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "connected, wait for location ..."))

    }
    private inner class GpsLocationListener: LocationListener, GpsStatus.Listener {

        override fun onLocationChanged(location: android.location.Location) {

            currentLocation = org.mackristof.followme.Location(location.time,
                    location.latitude,
                    location.longitude,
                    location.altitude,
                    (location.altitude - geoGrid?.GetAltitudeCorrection(location.latitude, location.longitude)!!),
                    location.accuracy,
                    gpsSatsAvailable)


            broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "located").putExtra(Constants.INTENT_LOCATION, currentLocation))
            Log.i(Constants.TAG, "location changed: (${location.latitude}, ${location.longitude} / atl : ${location.altitude}) with acc ${location.accuracy} on ${location.provider}")


        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            when (status) {
                LocationProvider.OUT_OF_SERVICE -> broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "GPS Out of Service"))
                LocationProvider.TEMPORARILY_UNAVAILABLE -> broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "GPS Temporarily Unavailable"))
                LocationProvider.AVAILABLE -> {
                }
            }
        }

        override fun onGpsStatusChanged(event: Int) {
            when (event) {
                GpsStatus.GPS_EVENT_SATELLITE_STATUS -> getNbSats()
            }
        }

        override fun onProviderDisabled(provider: String?) {
            Log.i(Constants.TAG, "gps disabled !!!")
        }

        override fun onProviderEnabled(provider: String?) {
            Log.i(Constants.TAG, "gps enabled !!!")
        }


        fun getNbSats() {
            val it = mlocManager?.getGpsStatus(null)?.getSatellites()?.iterator()
            if (it == null) {
                gpsSatsAvailable = 0;
            } else {
                var i: Int = 0;
                while (it.hasNext()) {
                    it.next()
                    i += 1
                }
                gpsSatsAvailable = i
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d(Constants.TAG, "onConnectionSuspended(): connection to location client suspended")
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this as com.google.android.gms.location.LocationListener )
        broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "gps connection suspended"))
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(Constants.TAG, "onConnectionFailed(): " + connectionResult?.errorMessage)
        broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "gps connection failed"))
    }


}



