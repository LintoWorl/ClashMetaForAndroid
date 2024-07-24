package com.github.kr328.clash.common.util

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeFormat {
    private const val FORMAT_YYYYMMDD = "yyyyMMdd"
    const val FORMAT_YYYY_MM_DD = "yyyy-MM-dd"

    private val defaultFormat: SimpleDateFormat
        get() = getDateFormat("yyyy-MM-dd HH:mm:ss")

    private val SDF_THREAD_LOCAL = ThreadLocal<SimpleDateFormat>()
    private fun getDateFormat(pattern: String): SimpleDateFormat {
        var simpleDateFormat = SDF_THREAD_LOCAL.get()
        if (simpleDateFormat == null) {
            simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            SDF_THREAD_LOCAL.set(simpleDateFormat)
        } else {
            simpleDateFormat.applyPattern(pattern)
        }
        return simpleDateFormat
    }

    fun millis2String(millis: Long = System.currentTimeMillis(), pattern: String): String {
        return millis2String(millis, getDateFormat(pattern))
    }

    fun millis2String(
        millis: Long = System.currentTimeMillis(),
        format: DateFormat = defaultFormat
    ): String {
        return format.format(Date(millis))
    }

    fun string2Millis(time: String, pattern: String): Long {
        return string2Millis(time, getDateFormat(pattern))
    }

    @JvmOverloads
    fun string2Millis(time: String, format: DateFormat = defaultFormat): Long {
        try {
            return format.parse(time)?.time ?: 0
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return -1
    }

    /**
     * 返回两个时间戳直接的日期间隔。
     */
    fun diffDaysBetween(startTime: Long, endTime: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.time = Date(startTime)
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return (endTime - calendar.timeInMillis) / (24 * 60 * 60 * 1000)
    }

    /**
     * 两个时间戳是否是相同日期
     */
    fun isSameDate(d1: Long, d2: Long): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = d1
        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = d2
        return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] && cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
    }

    @SuppressLint("SimpleDateFormat")
    fun convertDateToTimestamp(dateString: String, format: String = "yyyy/MM/dd"): Long {
        try {
            val dateFormat: DateFormat = SimpleDateFormat(format)
            val date = dateFormat.parse(dateString)
            if (date != null) {
                return date.time
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 根据时间戳，判断是否是今天
     */
    @SuppressLint("SimpleDateFormat")
    fun isToday(lastTime: Long): Boolean {
        if (lastTime == 0L) return false
        val today = SimpleDateFormat(FORMAT_YYYYMMDD).format(Date(System.currentTimeMillis()))
        val lastDay = SimpleDateFormat(FORMAT_YYYYMMDD).format(Date(lastTime))
        return today == lastDay
    }


    /**
     * 返回两个时间戳直接的日期间隔。
     */
    fun betweenDayToNow(startTime: Long, nowTime: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.time = Date(startTime)
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return (nowTime - calendar.timeInMillis) / (24 * 60 * 60 * 1000)
    }
}