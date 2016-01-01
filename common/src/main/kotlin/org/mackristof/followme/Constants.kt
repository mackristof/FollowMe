package org.mackristof.followme

class Constants {
    companion object {
        public  val COMMAND_PING = "/ping"
        public val COMMAND_START = "/start"
        public val COMMAND_STOP = "/stop"
        public val COMMAND_IS_GPS = "/gps"
        public val CONNECTION_TIME_OUT_MS: Long = 100
        public val INTENT_LOCATION = "location"
        public val INTENT_LOCATION_STATUS = "status"
        public val TAG = javaClass.`package`.name
        public val GPS_UPDATE_INTERVAL_MS: Long = 60000
        public val GPS_FASTEST_INTERVAL_MS: Long = 30000


    }
}