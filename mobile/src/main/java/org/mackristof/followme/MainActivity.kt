package org.mackristof.followme

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.wearable.Wearable
import org.mackristof.followme.message.ActivateGpsMsg
import org.mackristof.followme.message.AskGpsMsg
import org.mackristof.followme.message.PingMsg


class MainActivity: AppCompatActivity(), ConnectionCallbacks, OnConnectionFailedListener {


    var mStatusText: TextView? = null
    var nodeWearId: String? = null
    var mGoogleApiClient:GoogleApiClient? = null
    var wearableNodeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityInstance = this
        Log.i(Constants.TAG,"mainActivity created")
        setContentView(R.layout.activity_main)
        val mButtonStart = findViewById(R.id.mButtonStart) as Button
        mStatusText = findViewById(R.id.statusText) as TextView
        mGoogleApiClient = googleApiClient()
        mGoogleApiClient?.connect()

        mButtonStart.setOnClickListener { attemptStartTracking() }

    }


    fun attemptStartTracking() {
        fun connectWearable(){

            fun askGPS(nodeWearId:String) {
                fun gpsActivatedOnWear(nodeWearId: String){
                    Log.i(Constants.TAG,"GPS activated on Wear $nodeWearId")
                }

                fun startLocService(){
                    val intentLoc = Intent(this, GpsService::class.java)
                    if (!stopService(intentLoc)) {
                        startService(intentLoc)
                    }
                }

                fun activateWearGPS(nodeWearId: String){
                    ActivateGpsMsg(applicationContext,nodeWearId,null, ::gpsActivatedOnWear, ::startLocService).sendMessage()
                }

                AskGpsMsg(applicationContext,nodeWearId,null, ::activateWearGPS, ::startLocService ).sendMessage()
            }
            PingMsg(applicationContext,"hello", ::askGPS).sendMessage()

        }
        if (isWearableAPIExist()) {
            connectWearable()
        } else {
            mStatusText?.text = "wearable node not found "
        }
    }





    private fun googleApiClient(): GoogleApiClient? {
        return GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApiIfAvailable(Wearable.API)
                .addApi(LocationServices.API).build()
    }


    override fun onConnectionSuspended(cause: Int) {
        mStatusText?.text = "connection suspended cause: " + cause
    }

    override fun onConnectionFailed(result: ConnectionResult){
        if (result.errorCode == ConnectionResult.API_UNAVAILABLE) {
            mStatusText?.text = "no wearable api found"
        }
    }

    //google api connected
    override fun onConnected(context: Bundle?) {
        mStatusText?.text = "connected to google play api"
    }

    override fun onStop(){
        super.onStop()
        mGoogleApiClient?.disconnect()
    }

    override fun onPause(){
        super.onPause()

    }

    override fun onResume(){
        super.onResume()

    }

    private fun isWearableAPIExist(): Boolean {
        return (mGoogleApiClient!=null && (mGoogleApiClient as GoogleApiClient).hasConnectedApi(Wearable.API))
    }
    companion object{
        private var MainActivityInstance: MainActivity? = null
        fun getInstance(): MainActivity {
            if (MainActivityInstance!=null) {
                return MainActivityInstance as MainActivity
            } else {
                throw IllegalStateException("MainActivity instance is null")
            }
        }
    }
}
