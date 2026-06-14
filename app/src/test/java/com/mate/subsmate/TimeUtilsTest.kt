package com.mate.subsmate

import com.mate.subsmate.ui.utils.TimeUtils
import org.junit.Assert.*
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun `millis per second is correct`() {
        assertEquals(1000L, TimeUtils.MILLIS_PER_SECOND)
    }

    @Test
    fun `millis per minute is correct`() {
        assertEquals(60000L, TimeUtils.MILLIS_PER_MINUTE)
    }

    @Test
    fun `millis per hour is correct`() {
        assertEquals(3600000L, TimeUtils.MILLIS_PER_HOUR)
    }

    @Test
    fun `millis per day is correct`() {
        assertEquals(86400000L, TimeUtils.MILLIS_PER_DAY)
    }

    @Test
    fun `default custom cycle days is 30`() {
        assertEquals(30, TimeUtils.DEFAULT_CUSTOM_CYCLE_DAYS)
    }

    @Test
    fun `millis per day multiplied by 7 equals one week`() {
        assertEquals(7 * TimeUtils.MILLIS_PER_DAY, 7 * 86400000L)
    }
}
