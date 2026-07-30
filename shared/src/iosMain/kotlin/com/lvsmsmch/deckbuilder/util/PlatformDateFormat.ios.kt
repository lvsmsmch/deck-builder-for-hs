package com.lvsmsmch.deckbuilder.util

import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatMonthYear(year: Int, month: Int): String {
    val components = NSDateComponents().apply {
        setYear(year.toLong())
        setMonth(month.toLong())
        setDay(1)
    }
    val date = NSCalendar.currentCalendar.dateFromComponents(components) ?: return "$month.$year"
    val fmt = NSDateFormatter().apply {
        locale = NSLocale.currentLocale
        dateFormat = "LLLL yyyy"
    }
    val text = fmt.stringFromDate(date)
    return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

actual fun formatDateTime(epochMs: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(epochMs / 1000.0)
    val fmt = NSDateFormatter().apply {
        locale = NSLocale.currentLocale
        dateStyle = NSDateFormatterMediumStyle
        timeStyle = NSDateFormatterShortStyle
    }
    return fmt.stringFromDate(date)
}
