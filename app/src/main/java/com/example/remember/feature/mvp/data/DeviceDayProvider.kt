package com.example.remember.feature.mvp.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/** Resolves the calendar day from the date, time and time zone configured on the device. */
object DeviceDayProvider {
    fun currentEpochDay(): Long = LocalDate.now(ZoneId.systemDefault()).toEpochDay()

    fun currentLocalHour(): Int = ZonedDateTime.now(ZoneId.systemDefault()).hour

    fun monthKey(epochDay: Long = currentEpochDay()): String {
        val date = LocalDate.ofEpochDay(epochDay)
        return "%04d-%02d".format(date.year, date.monthValue)
    }

    fun startOfDayMillis(epochDay: Long): Long = LocalDate.ofEpochDay(epochDay)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    internal fun epochDayAt(instant: Instant, zoneId: ZoneId): Long =
        instant.atZone(zoneId).toLocalDate().toEpochDay()
}
