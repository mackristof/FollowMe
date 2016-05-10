package org.mackristof.followme

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.wearable.Wearable


class GpsService: Service(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {



    var mGoogleApiClient: GoogleApiClient? = null
    var broadcaster: LocalBroadcastManager? = null
    var currentLocation: Location? = null

    val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(Constants.GPS_UPDATE_INTERVAL_MS)
            .setFastestInterval(Constants.GPS_FASTEST_INTERVAL_MS)

    override fun onCreate(){
        Log.i(Constants.TAG,"gpsService created")
    }

    override fun onDestroy(){
        Log.i(Constants.TAG,"gpsService stopped ")
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this)
        mGoogleApiClient?.disconnect()
    }

    override fun onStartCommand(intent:Intent , flags: Int, startId: Int ):Int {
        Log.i(Constants.TAG,"gpsService started ")
        mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                .addApiIfAvailable(LocationServices.API)
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        mGoogleApiClient?.connect()
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
        if (Utils.hasGPS(this.applicationContext)) {


            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                    .setResultCallback(ResultCallback<Status> { status ->
                        if (status.status.isSuccess) {
                            if (Log.isLoggable(Constants.TAG, Log.DEBUG)) {
                                Log.d(Constants.TAG, "Successfully requested location updates")
                            }
                        } else {
                            Log.e(Constants.TAG,
                                    "Failed in requesting location updates, status code: ${status.statusCode}, message: ${status.statusMessage} ")
                        }
                    })
        } else {
            //TODO send message to start gps on other side

        }
    }

    override fun onLocationChanged(location: Location?) {
        currentLocation = location
        broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "located").putExtra(Constants.INTENT_LOCATION, "${location?.latitude},${location?.longitude} / ${location?.altitude} / ${location?.accuracy}"))
        Log.i(Constants.TAG,"location changed: (${location?.latitude}, ${location?.longitude} / atl : ${location?.altitude}) with acc ${location?.accuracy} on ${location?.provider}")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d(Constants.TAG, "onConnectionSuspended(): connection to location client suspended")
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this as com.google.android.gms.location.LocationListener )
        broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "gps connection suspended"))
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(Constants.TAG, "onConnectionFailed(): " + connectionResult?.errorMessage)
        broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "gps connection failed"))
    }

}
