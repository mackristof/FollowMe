package org.mackristof.followme

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

class Utils {

    companion object {
        fun isRunningOnWatch(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
        }

        fun hasGPS(context: Context): Boolean {
            return (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))
        }

        fun isGpsEnabled(context: Context):Boolean{
           return  (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        fun isServiceRunning(context: Context, serviceName: String?): Boolean{
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in actManager.getRunningServices(Int.MAX_VALUE)){
                if (serviceName.equals(service.service.className)){
                    return true
                }
            }
            return false
        }
    }


}

