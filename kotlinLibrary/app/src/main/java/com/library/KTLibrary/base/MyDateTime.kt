package com.library.KTLibrary.base

import com.library.KTLibrary.myEnum.DateType
import com.library.KTLibrary.myEnum.TimePeriod
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object MyDateTime {
    fun getCurrentDateTime(dateType: DateType): String {
        return when (dateType) {
            DateType.DATE -> LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            DateType.TIME -> LocalTime.now().format(getFormatter(dateType))
            DateType.HOUR -> LocalTime.now().format(getFormatter(dateType)).split(":").first()
            DateType.DATETIME -> LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }

    fun getFormatter(dateType: DateType): DateTimeFormatter = when (dateType) {
        DateType.DATE -> DateTimeFormatter.ofPattern("yyyy-MM-dd")
        DateType.TIME -> DateTimeFormatter.ofPattern("HH:mm:ss")
        DateType.DATETIME -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        else -> DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    fun dateCalculate(
        date: String,
        day: Int,
        shouldAdd: Boolean,
        period: TimePeriod
    ): String {
        val localDate = LocalDate.parse(date)
        val adjustedDate = calcDate(localDate, day, shouldAdd, period)
        return adjustedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun calcDate(
        localDate: LocalDate,
        day: Int,
        shouldAdd: Boolean,
        period: TimePeriod
    ): LocalDate {
        return when(period) {
            TimePeriod.DAY -> if(shouldAdd) localDate.plusDays(day.toLong()) else localDate.minusDays(day.toLong())
            TimePeriod.WEEK -> if(shouldAdd) localDate.plusWeeks(day.toLong()) else localDate.minusWeeks(day.toLong())
            TimePeriod.MONTH -> if(shouldAdd) localDate.plusMonths(day.toLong()) else localDate.minusMonths(day.toLong())
            TimePeriod.YEAR -> if(shouldAdd) localDate.plusYears(day.toLong()) else localDate.minusYears(day.toLong())
        }
    }

    fun formatToDisplay(targetDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        val date = inputFormat.parse(targetDate)
        return outputFormat.format(date ?: return "")
    }

    fun dateCalculate(date: String, days: Int, shouldAdd: Boolean, chronoUnit: ChronoUnit): String {
        val localDate = LocalDate.parse(date)
        val adjustedDate = if (shouldAdd) localDate.plus(days.toLong(), chronoUnit) else localDate.minus(days.toLong(), chronoUnit)
        return adjustedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    fun findNumDay(date: String): Int {
        val localDate = LocalDate.parse(date)
        return localDate.lengthOfMonth()
    }

    fun changeDateFormat(dateString: String, yearFlag: Boolean): String {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        return if (yearFlag) {
            date.format(DateTimeFormatter.ofPattern("yy-MM"))
        } else {
            date.format(DateTimeFormatter.ofPattern("MM-dd"))
        }
    }

    fun getDateFormat(): DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getTimeZone(): String {
        val zoneId = ZoneId.systemDefault()
        val zonedDateTime = ZonedDateTime.now(zoneId)
        val offsetString = DateTimeFormatter.ofPattern("xxx").format(zonedDateTime.offset)

        val currentCountryCode = Locale.getDefault().country
        return "$offsetString/${zoneId.id}/$currentCountryCode"
    }
}