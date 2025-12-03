package ru.kvf.callblocker.data

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.reflect.Type
import java.util.Date


object ContactsStorage {
    private const val APP_PREFS_NAME = "app_prefs"
    private const val CONTACTS_KEY = "contacts_key"
    private const val BLOCKED_CALLS_KEY = "blocked_calls_key"
    private const val IS_BLOCKING_ENABLE_KEY = "is_blocking_enable_key"

    val blockedCallsFlow = MutableStateFlow<List<BlockedCall>>(emptyList())
    val blockedCallsEnableFlow = MutableStateFlow(true)

    val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(Date::class.java, DateConverter())
            .create()
    }
    private val contactListType = object : TypeToken<List<Contact>>() {}.type
    private val blockedCallsType = object : TypeToken<List<BlockedCall>>() {}.type

    fun subscribeToBlockedCalls(context: Context) {
        val calls = loadBlockedCalls(context)
        blockedCallsFlow.value = calls
        val prefs = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener { prefs, key ->
            if (key == BLOCKED_CALLS_KEY) {
                val calls = loadBlockedCalls(context)
                blockedCallsFlow.value = calls
            } else if (key == IS_BLOCKING_ENABLE_KEY) {
                val isEnable = prefs.getBoolean(IS_BLOCKING_ENABLE_KEY, true)
                blockedCallsEnableFlow.value = isEnable
            }
        }
    }

    fun saveContacts(context: Context, contacts: List<Contact>) {
        val json = gson.toJson(contacts)
        val prefs = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(CONTACTS_KEY, json) }
    }

    fun loadContacts(context: Context): List<Contact> {
        val prefs = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(CONTACTS_KEY, null)
        return json?.let { gson.fromJson(it, contactListType) } ?: emptyList()
    }

    fun loadContactPhones(context: Context) = loadContacts(context).map { it.phone }

    private fun loadBlockedCalls(context: Context): List<BlockedCall> {
        val prefs = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(BLOCKED_CALLS_KEY, null)
        val calls: List<BlockedCall> =
            json?.let { gson.fromJson(it, blockedCallsType) } ?: emptyList()
        val sortedCalls = calls.sortedByDescending { it.date }
        return sortedCalls
    }

    fun addBlockedCall(context: Context, call: BlockedCall) {
        val calls = loadBlockedCalls(context).toMutableList()
        calls.add(call)
        val json = gson.toJson(calls)
        val prefs = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(BLOCKED_CALLS_KEY, json) }
    }

    fun changeIsBlockingEnable(context: Context) {
        val prefs = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
        val currentValue = prefs.getBoolean(IS_BLOCKING_ENABLE_KEY, true)
        prefs.edit {
            putBoolean(IS_BLOCKING_ENABLE_KEY, !currentValue)
        }
    }
}


private class DateConverter : JsonSerializer<Date>, JsonDeserializer<Date> {
    override fun serialize(
        src: Date,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return com.google.gson.JsonPrimitive(src.time)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Date {
        return json.asLong.let(::Date)
    }
}