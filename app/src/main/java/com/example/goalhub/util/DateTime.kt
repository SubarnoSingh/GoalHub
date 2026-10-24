package com.example.goalhub.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

/** Short "Aug 15" style label. */
val shortDate: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

/** "Aug 15, 2026" style label. */
val mediumDate: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

/** "Saturday, August 15" style label. */
val fullDate: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)

fun LocalDate.toEpochDayLong(): Long = toEpochDay()

fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this)

/** M3 DatePicker works in UTC millis — convert explicitly. */
fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.fromUtcMillis(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

fun nowMillis(): Long = System.currentTimeMillis()

/** "Friday, August 15" for headers, "Aug 15" for chips. */
fun LocalDate.formatFull(): String = fullDate.format(this)
fun LocalDate.formatShort(): String = shortDate.format(this)
fun LocalDate.formatMedium(): String = mediumDate.format(this)

/** "Aug 15 → Aug 20" range label. */
fun LocalDate.rangeLabel(end: LocalDate): String = "${formatShort()} → ${end.formatShort()}"

/** Inclusive day count of a range. */
fun LocalDate.daysUntil(other: LocalDate): Long = ChronoUnit.DAYS.between(this, other)

/** "12 days left" / "Due today" / "3 days overdue" — relative to [today]. */
fun LocalDate.relativeLabel(today: LocalDate): String {
    val days = today.daysUntil(this)
    return when {
        days < 0 -> "${-days} day${if (-days == 1L) "" else "s"} overdue"
        days == 0L -> "Due today"
        days == 1L -> "1 day left"
        else -> "$days days left"
    }
}

/** Weekday index with Monday = 0 (for calendar grids). */
fun LocalDate.mondayBasedIndex(): Int = (dayOfWeek.value + 6) % 7

fun LocalDate.toDeviceZoneMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
