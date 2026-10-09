package dev.therealashik.jules.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatterTest {

    @Test
    fun testFormatSessionTimestampWithValidIso() {
        val formatted = formatSessionTimestamp("2026-05-05T17:16:18.123456Z")
        assertEquals("May 5 · 5:16 PM", formatted)
    }

    @Test
    fun testFormatSessionTimestampWithMorningTime() {
        val formatted = formatSessionTimestamp("2026-10-09T08:05:00Z")
        assertEquals("Oct 9 · 8:05 AM", formatted)
    }

    @Test
    fun testFormatSessionTimestampWithBlankInput() {
        val formatted = formatSessionTimestamp("")
        assertEquals("", formatted)
    }
}
