package ru.kvf.callblocker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.app.NotificationCompat
import ru.kvf.callblocker.data.BlockedCall
import ru.kvf.callblocker.data.ContactsStorage

private const val CALL_BLOCK_CHANNEL = "CALL_BLOCK_CHANNEL"

class MyCallScreeningService : CallScreeningService() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart
        val filteredNumber = number.filterNumber()
        val isAllowed = isNumberAllowed(filteredNumber)
        if (!isAllowed) {
            ContactsStorage.addBlockedCall(this, BlockedCall(phone = filteredNumber))
            showBlockedCallNotification(filteredNumber)
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

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CALL_BLOCK_CHANNEL,
            "Блокировка звонков",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Уведомления о заблокированных звонках"
            enableVibration(true)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

    }

    private fun showBlockedCallNotification(phone: String?) {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CALL_BLOCK_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_call) // или ваша иконка
            .setContentTitle("Звонок заблокирован")
            .setContentText("Номер: $phone")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(phone.hashCode(), notification)
    }
}

fun String?.filterNumber(): String? = this?.replace(Regex("[^+\\d]"), "")
