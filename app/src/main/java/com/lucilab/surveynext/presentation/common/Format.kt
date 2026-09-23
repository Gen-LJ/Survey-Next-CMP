package com.lucilab.surveynext.presentation.common

import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

private fun parse(iso: String?): OffsetDateTime? =
    iso?.let { runCatching { OffsetDateTime.parse(it) }.getOrNull() }

/** "Sep 23, 2026" for an RFC 3339 timestamp from the backend. */
fun formatDate(iso: String?): String =
    parse(iso)?.format(dateFormatter) ?: "—"

/** "just now", "5m ago", "3h ago", "2d ago", then the date. */
fun formatRelative(iso: String?): String {
    val time = parse(iso) ?: return "—"
    val elapsed = Duration.between(time, OffsetDateTime.now())
    return when {
        elapsed.isNegative || elapsed.toMinutes() < 1 -> "just now"
        elapsed.toHours() < 1 -> "${elapsed.toMinutes()}m ago"
        elapsed.toDays() < 1 -> "${elapsed.toHours()}h ago"
        elapsed.toDays() < 7 -> "${elapsed.toDays()}d ago"
        else -> time.format(dateFormatter)
    }
}

fun formatPoints(points: Int): String = "%,d".format(points)

fun formatPoints(points: Long): String = "%,d".format(points)

fun pluralize(count: Int, singular: String, plural: String = "${singular}s") =
    "$count ${if (count == 1) singular else plural}"
