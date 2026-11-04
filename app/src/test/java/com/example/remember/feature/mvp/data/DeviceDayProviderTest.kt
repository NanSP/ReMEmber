package com.example.remember.feature.mvp.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceDayProviderTest {
    @Test
    fun `same instant follows the phone time zone calendar day`() {
        val instant = Instant.parse("2026-10-30T01:30:00Z")

        val bahiaDay = DeviceDayProvider.epochDayAt(instant, ZoneId.of("America/Bahia"))
        val utcDay = DeviceDayProvider.epochDayAt(instant, ZoneId.of("UTC"))

        assertEquals(LocalDate.of(2026, 10, 29).toEpochDay(), bahiaDay)
        assertEquals(LocalDate.of(2026, 10, 30).toEpochDay(), utcDay)
    }
}
