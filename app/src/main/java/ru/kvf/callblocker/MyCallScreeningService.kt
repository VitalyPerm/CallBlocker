package ru.kvf.callblocker

import android.telecom.Call
import android.telecom.CallScreeningService
import ru.kvf.callblocker.data.BlockedCall
import ru.kvf.callblocker.data.ContactsStorage

class MyCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart
        val filteredNumber = number.filterNumber()
        val isAllowed = isNumberAllowed(filteredNumber)
        if (!isAllowed) {
            ContactsStorage.addBlockedCall(this, BlockedCall(phone = filteredNumber))
        }

        if (callDetails.callDirection == Call.Details.DIRECTION_INCOMING) {
            val response = CallResponse.Builder()
                .setDisallowCall(!isAllowed)
                .setRejectCall(!isAllowed)
                .build()
            respondToCall(callDetails, response)
        }
    }

    private fun isNumberAllowed(number: String?): Boolean {
        val phonesList = ContactsStorage.loadContactPhones(this)
        return number in phonesList
    }
}

fun String?.filterNumber(): String? = this?.replace(Regex("[^+\\d]"), "")
