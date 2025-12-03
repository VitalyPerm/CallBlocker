package ru.kvf.callblocker

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class MyCallScreeningService : CallScreeningService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("check___", "MyCallScreeningService serviceRunning")
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart
        val isAllowed = isNumberAllowed(number)

        Log.d("check___", "$number $isAllowed")

        if (callDetails.callDirection == Call.Details.DIRECTION_INCOMING) {
            Log.d("check___", "callDetails.callDirection == Call.Details.DIRECTION_INCOMING")
            val response = CallResponse.Builder()
                .setDisallowCall(!isAllowed)
                .setRejectCall(!isAllowed)
                .build()
            respondToCall(callDetails, response)
        }
    }

    private fun isNumberAllowed(number: String?): Boolean {
        val allowList = setOf("+79197102196")

        return number in allowList
    }
}
