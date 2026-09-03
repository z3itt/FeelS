package com.feels.core.domain.util

import java.util.Calendar

object LocalDayClock {
    fun startOfDay(timestampMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestampMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun startOfDayDaysAgo(daysAgo: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startOfDay()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo.coerceAtLeast(0))
        return calendar.timeInMillis
    }

    fun calendarDaysBetween(earlierStartOfDay: Long, laterStartOfDay: Long): Int {
        if (earlierStartOfDay >= laterStartOfDay) return 0
        val cursor = Calendar.getInstance()
        cursor.timeInMillis = earlierStartOfDay
        var days = 0
        while (cursor.timeInMillis < laterStartOfDay) {
            cursor.add(Calendar.DAY_OF_YEAR, 1)
            days++
        }
        return days
    }
}
