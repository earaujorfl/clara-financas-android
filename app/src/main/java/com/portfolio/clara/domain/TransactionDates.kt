package com.portfolio.clara.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Stores the selected calendar day in the existing Room timestamp column. */
object TransactionDates {
    fun atStartOfDayMillis(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun localDate(timestampMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(timestampMillis).atZone(zone).toLocalDate()
}
