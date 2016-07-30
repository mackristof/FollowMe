package org.mackristof.followme

import android.app.Fragment
import android.app.FragmentManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.mackristof.followme.fragment.DisplayFragment
import org.mackristof.followme.fragment.StartFragment
import org.mackristof.followme.service.WearMessageListener
import java.text.SimpleDateFormat
import java.util.*

class MainWearActivity : WearableActivity(){









    private inner class SampleGridPagerAdapter : FragmentGridPagerAdapter {
        val mRows: ArrayList<SampleGridPagerAdapter.Row> = ArrayList<SampleGridPagerAdapter.Row>()
        var mCtx: Context? = null
        constructor(ctx : Context, fm: FragmentManager) : super(fm) {
            mCtx = ctx
            val firstRow = Row(StartFragment(), DisplayFragment())
            mRows.add(firstRow)
            mRows.add(Row(cardFragment(R.string.card_start_title, R.string.card_start_text)))
        }
        override fun getRowCount(): Int {
            return mRows.size
        }

        override fun getColumnCount(rowNum: Int): Int {
            return mRows[rowNum].getColumnCount();
        }

        override fun getFragment(row: Int, col: Int): Fragment? {
             return mRows[row]?.getColumn(col)
        }

        fun cardFragment(titleRes: Int, textRes: Int): Fragment{
            val res = mCtx?.resources
            val fragment = CardFragment.create(res?.getText(titleRes), res?.getText(textRes))
            return fragment
        }
        private inner class Row(vararg fragments: Fragment) {
            internal val columns: MutableList<Fragment> = ArrayList()

            init {
                for (f in fragments) {
                    add(f)
                }
            }

            fun add(f: Fragment) {
                columns.add(f)
            }

            internal fun getColumn(i: Int): Fragment {
                return columns[i]
            }

            fun getColumnCount(): Int {
                return columns.size
            }
        }


    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainWearActivityInstance = this
        setContentView(R.layout.activity_main)
        setAmbientEnabled()
        val pager =  findViewById(R.id.pager) as GridViewPager
        pager.setAdapter(SampleGridPagerAdapter(this, getFragmentManager()));
        var dotsPageIndicator = findViewById(R.id.page_indicator) as DotsPageIndicator
        dotsPageIndicator.setPager(pager)

        val intentMsg = Intent(this, WearMessageListener::class.java)
        if (!stopService(intentMsg)) {
            startService(intentMsg)
        }






    }




    override fun onStop(){
        val intentLoc = Intent(this, GpsService::class.java)
        stopService(intentLoc)

        super.onStop()
        //TODO stop service

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





    companion object {
        private val AMBIENT_DATE_FORMAT = SimpleDateFormat("HH:mm", Locale.US)
        //val LOCATION = "location"
        //val STATUS = "status"
        val TAG: String = MainWearActivity::class.java.simpleName
        private var MainWearActivityInstance: MainWearActivity? = null
        fun getInstance(): MainWearActivity {
            if (MainWearActivityInstance !=null) {
                return MainWearActivityInstance as MainWearActivity
            } else {
                throw IllegalStateException("MainWearActivity instance is null")
            }
        }
    }
}
