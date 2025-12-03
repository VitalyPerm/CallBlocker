package ru.kvf.callblocker.data

import java.util.Date

data class BlockedCall(
    val phone: String?,
    val date: Date = Date(),
)