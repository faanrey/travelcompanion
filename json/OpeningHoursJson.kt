package com.example.egypttravel.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * JSON wire format for a POI's opening hours.
 *
 * `weeklySchedule` is a map keyed by day-of-week strings (MONDAY..SUNDAY),
 * each mapping to a [DayScheduleJson]. Day schedules are polymorphic: a
 * single `type` discriminator selects between Closed / AlwaysOpen / Hours.
 *
 * The Moshi adapter for [DayScheduleJson] is registered manually in
 * NetworkModule via PolymorphicJsonAdapterFactory because Moshi's codegen
 * doesn't handle sealed-class polymorphism with a discriminator field.
 */
@JsonClass(generateAdapter = true)
data class OpeningHoursJson(
    @Json(name = "weeklySchedule") val weeklySchedule: Map<String, DayScheduleJson>,
    @Json(name = "notes") val notes: LocalizedTextJson? = null
)

/**
 * Polymorphic day schedule. The `type` field discriminates the variant:
 *  - "closed"      -> [DayScheduleJson.Closed]
 *  - "alwaysOpen"  -> [DayScheduleJson.AlwaysOpen]
 *  - "hours"       -> [DayScheduleJson.Hours]
 *
 * `open` and `close` are HH:mm 24-hour strings ("00:00" is midnight, allowed
 * as a closing time meaning "until midnight").
 */
sealed class DayScheduleJson {

    @JsonClass(generateAdapter = true)
    data class Closed(
        @Json(name = "type") val type: String = "closed"
    ) : DayScheduleJson()

    @JsonClass(generateAdapter = true)
    data class AlwaysOpen(
        @Json(name = "type") val type: String = "alwaysOpen"
    ) : DayScheduleJson()

    @JsonClass(generateAdapter = true)
    data class Hours(
        @Json(name = "type") val type: String = "hours",
        @Json(name = "open") val open: String,
        @Json(name = "close") val close: String
    ) : DayScheduleJson()
}
