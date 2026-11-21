package com.portfolio.clara.domain

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionDatesTest {
    private val brazil = ZoneId.of("America/Sao_Paulo")

    @Test fun selectedCalendarDateSurvivesTimestampRoundTrip() {
        val date = LocalDate.of(2026, 11, 5)
        val time = TransactionDates.atStartOfDayMillis(date, brazil)
        assertEquals(date, TransactionDates.localDate(time, brazil))
    }

    @Test fun datesInDifferentMonthsStayInTheirSelectedMonth() {
        val october = LocalDate.of(2026, 10, 31)
        val november = LocalDate.of(2026, 11, 1)
        assertEquals(10, TransactionDates.localDate(TransactionDates.atStartOfDayMillis(october, brazil), brazil).monthValue)
        assertEquals(11, TransactionDates.localDate(TransactionDates.atStartOfDayMillis(november, brazil), brazil).monthValue)
    }
}
