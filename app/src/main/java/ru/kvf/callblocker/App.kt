package ru.kvf.callblocker

import android.app.Application
import android.content.SharedPreferences
import ru.kvf.callblocker.data.ContactsStorage

class App : Application() {

    companion object {
        lateinit var PREFERENCES: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        PREFERENCES = getSharedPreferences(ContactsStorage.APP_PREFS_NAME, MODE_PRIVATE)
    }
}