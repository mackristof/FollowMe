package org.mackristof.followme.message

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.MessageApi

/**
 * Created by christophem on 31/12/15.
 */
interface Msg: GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    val path: String
    val text: String?
}