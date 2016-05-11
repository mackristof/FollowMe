package org.mackristof.followme.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable
import org.mackristof.followme.Constants

/**
 * Created by christophem on 14/01/2016.
 */
class LocationPublisherService: Service(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {
    override fun onConnectionFailed(p0: ConnectionResult) {
        throw UnsupportedOperationException()
    }


    var mGoogleApiClient: GoogleApiClient? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(Constants.TAG,"locationPublisher started ")
        mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        mGoogleApiClient?.connect()
        return super.onStartCommand(intent, flags, startId)
    }



    override fun onConnected(p0: Bundle?) {
        throw UnsupportedOperationException()
        //TODO what can do on this event ??
    }

    override fun onConnectionSuspended(p0: Int) {
        throw UnsupportedOperationException()
        //TODO what can do on this event ??
    }


}