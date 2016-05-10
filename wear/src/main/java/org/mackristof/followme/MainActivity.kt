package org.mackristof.followme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.BoxInsetLayout
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.mackristof.followme.service.WearMessageListener
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : WearableActivity(){

    private var mContainerView: BoxInsetLayout? = null
    private var mTextView: TextView? = null
    private var mClockView: TextView? = null
    private var mButtonStart: Button? = null
    private var mButtonStop: Button? = null
    private var broadcaster: LocalBroadcastManager? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityInstance = this
        setContentView(R.layout.activity_main)
        setAmbientEnabled()

        val intentMsg = Intent(this, WearMessageListener::class.java)
        if (!stopService(intentMsg)) {
            startService(intentMsg)
        }

        if (Utils.hasGPS(this)) {
            mButtonStart?.visibility = View.VISIBLE

        } else {
            Toast.makeText(this, "device without GPS. try to start on smartphone", Toast.LENGTH_LONG)
            mButtonStart?.visibility = View.INVISIBLE
            Log.e(TAG, "not enough permission for application with GPS location")
        }

        mContainerView = findViewById(R.id.container) as BoxInsetLayout
        mTextView = findViewById(R.id.text) as TextView
        mClockView = findViewById(R.id.clock) as TextView
        mButtonStart = findViewById(R.id.startButton) as Button
        mButtonStop = findViewById(R.id.stopButton) as Button
        mButtonStart!!.setOnClickListener(View.OnClickListener {
            // start service Location
            val intentLoc = Intent(this, GpsService::class.java)
            intentLoc.putExtra(Constants.INTENT_LOCATION_EXTRA_PUBLISH,true)
            if (!stopService(intentLoc)) {
                startService(intentLoc)
            }


        })

        mButtonStop!!.setOnClickListener(View.OnClickListener {
            //TODO start service
            val intent = Intent(this, GpsService::class.java)
            stopService(intent)
        })

        broadcaster = LocalBroadcastManager.getInstance(this)
        broadcaster?.registerReceiver(LocationBroadcastReceiver(), IntentFilter(LOCATION))


    }

    private class LocationBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.hasExtra(LOCATION)){
                MainActivity.getInstance().updateDisplay(intent.getStringExtra(LOCATION))
            } else {
                MainActivity.getInstance().updateDisplay(intent.getStringExtra(STATUS))
            }
        }

    }


    override fun onStop(){
        val intentLoc = Intent(this, GpsService::class.java)
        stopService(intentLoc)
        broadcaster?.unregisterReceiver(LocationBroadcastReceiver())
        super.onStop()
        //TODO stop service

    }

    override fun onPause(){
        super.onPause()
        broadcaster?.unregisterReceiver(LocationBroadcastReceiver())
    }

    override fun onResume(){
        super.onResume()
        if (Utils.isServiceRunning(this, GpsService::class.java.name)){
            broadcaster?.registerReceiver(LocationBroadcastReceiver(), IntentFilter(LOCATION))
        }
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
//        updateDisplay("ambient")
    }

    override fun onUpdateAmbient() {
        super.onUpdateAmbient()
//        updateDisplay("update ambient")
    }

    override fun onExitAmbient() {
//        updateDisplay("exitAmbient")
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
        val LOCATION = "location"
        val STATUS = "status"
        val TAG: String = MainActivity::class.java.simpleName
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
