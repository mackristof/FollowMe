package org.mackristof.followme

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by christophem on 01/07/2016.
 */
data class Location (val timestamp: Long, val lat: Double, val lon: Double, val alt: Double, val corAlt: Double, val acc: Float): Parcelable {

    constructor(source: Parcel): this(source.readLong(),source.readDouble(), source.readDouble(), source.readDouble(), source.readDouble(), source.readFloat())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeLong(this.timestamp)
        dest?.writeDouble(this.lat)
        dest?.writeDouble(this.lon)
        dest?.writeDouble(this.alt)
        dest?.writeDouble(this.corAlt)
        dest?.writeFloat(this.acc)
    }

    companion object {
        @JvmField final val CREATOR: Parcelable.Creator<Location> = object : Parcelable.Creator<Location> {
            override fun createFromParcel(source: Parcel): Location{
                return Location(source)
            }

            override fun newArray(size: Int): Array<Location?> {
                return arrayOfNulls(size)
            }
        }
    }
}