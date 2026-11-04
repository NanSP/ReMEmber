package com.example.remember.feature.mvp.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Resolves the calendar day from the date, time and time zone configured on the device. */
object DeviceDayProvider {
    fun currentEpochDay(): Long = LocalDate.now(ZoneId.systemDefault()).toEpochDay()

    internal fun epochDayAt(instant: Instant, zoneId: ZoneId): Long =
        instant.atZone(zoneId).toLocalDate().toEpochDay()
}
