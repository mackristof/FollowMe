package org.mackristof.followme

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.support.v4.app.ActivityCompat

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
    }


}

