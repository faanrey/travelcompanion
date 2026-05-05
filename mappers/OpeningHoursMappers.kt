package com.example.egypttravel.data.mapper

import com.example.egypttravel.data.remote.dto.DayScheduleJson
import com.example.egypttravel.data.remote.dto.OpeningHoursJson
import com.example.egypttravel.domain.model.DaySchedule
import com.example.egypttravel.domain.model.OpeningHours
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Mappers between [OpeningHoursJson] (wire) and [OpeningHours] (domain).
 *
 * The schedule map is keyed by uppercase day-of-week strings (MONDAY..SUNDAY)
 * matching [DayOfWeek.name]. Unknown keys are ignored rather than throwing —
 * defensive parsing for content authored by hand.
 *
 * Time strings are HH:mm. A close time of "00:00" represents midnight (end of
 * day) and is left as LocalTime.MIDNIGHT; UI code that displays hours should
 * treat MIDNIGHT close as "until midnight" rather than "closed at start of day."
 */

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

internal fun OpeningHoursJson.toDomain(): OpeningHours {
    val parsed = weeklySchedule.mapNotNull { (dayKey, scheduleJson) ->
        val day = runCatching { DayOfWeek.valueOf(dayKey.uppercase()) }.getOrNull()
            ?: return@mapNotNull null
        day to scheduleJson.toDomain()
    }.toMap()

    return OpeningHours(
        weeklySchedule = parsed,
        notes = notes?.toDomain()
    )
}

internal fun DayScheduleJson.toDomain(): DaySchedule = when (this) {
    is DayScheduleJson.Closed -> DaySchedule.Closed
    is DayScheduleJson.AlwaysOpen -> DaySchedule.AlwaysOpen
    is DayScheduleJson.Hours -> DaySchedule.Hours(
        open = LocalTime.parse(open, TIME_FORMAT),
        close = LocalTime.parse(close, TIME_FORMAT)
    )
}
