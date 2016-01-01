package org.mackristof.followme.service

import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import org.mackristof.followme.MainActivity
import org.mackristof.followme.Constants
import org.mackristof.followme.Utils
import java.util.concurrent.TimeUnit

class WearMessageListener: WearableListenerService() {

    override fun onCreate(){
        super.onCreate()
        Log.i(MainActivity.TAG,"WearableListenerService started")
    }

    override fun onMessageReceived(messageEvent: MessageEvent){
        if (messageEvent.path == Constants.COMMAND_PING) {
            Log.i(MainActivity.TAG, messageEvent.path + " (" + String(messageEvent.data) + ")")
            reply(Constants.COMMAND_PING, "pong",messageEvent.sourceNodeId)
        } else if (messageEvent.path == Constants.COMMAND_IS_GPS) {
            reply(Constants.COMMAND_IS_GPS,(Utils.hasGPS(applicationContext) && Utils.isGpsEnabled(applicationContext)).toString(),messageEvent.sourceNodeId)
        } else {
            super.onMessageReceived( messageEvent )
        }
    }

    private fun reply(path: String, message: String?, senderId: String) {
        val client = GoogleApiClient.Builder(applicationContext)
                .addApi(Wearable.API)
                .build()
        client.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        Wearable.MessageApi.sendMessage(client, senderId, path, message?.toByteArray());
        client.disconnect();
    }

}