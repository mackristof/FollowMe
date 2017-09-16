package org.mackristof.followme.fragment

import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import org.mackristof.followme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by christophem on 29/07/2016.
 */

class StartFragment : Fragment() {


    //    private var mClockView: TextView? = null
    private var mButtonStart: Button? = null
    private var mButtonStop: Button? = null
    override fun  onCreateView( inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState:  Bundle?): View? {
        val view = inflater?.inflate(R.layout.start_stop_fragment, container, false)!!


        mButtonStart = view.findViewById(R.id.startButton) as Button
        mButtonStop = view.findViewById(R.id.stopButton) as Button
        mButtonStart?.setOnClickListener(View.OnClickListener {
            // start service Location
            val intentLoc = Intent(this.context, GpsService::class.java)
            intentLoc.putExtra(Constants.INTENT_LOCATION_EXTRA_PUBLISH,true)
            if (!Utils.isServiceRunning(context,GpsService::class.java.name)) {
                activity.startService(intentLoc)
            }
        })

        mButtonStop?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this.context, GpsService::class.java)
            activity.stopService(intent)
        })


        return view
    }



}
