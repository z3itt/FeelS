package com.feels.core.domain.util

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalDayClockTest {

    @Test
    fun startOfDayClearsClockFields() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = LocalDayClock.startOfDay()
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun calendarDaysBetweenSameMorningIsZero() {
        val start = LocalDayClock.startOfDay()
        assertEquals(0, LocalDayClock.calendarDaysBetween(start, start))
    }

    @Test
    fun startOfDayDaysAgoIsNotAfterToday() {
        val today = LocalDayClock.startOfDay()
        val weekAgo = LocalDayClock.startOfDayDaysAgo(6)
        assertTrue(weekAgo <= today)
        assertEquals(6, LocalDayClock.calendarDaysBetween(weekAgo, today))
    }
}
