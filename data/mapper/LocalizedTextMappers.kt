package com.example.egypttravel.data.mapper

import com.example.egypttravel.data.remote.dto.LocalizedTextJson
import com.example.egypttravel.domain.model.LocalizedText

/**
 * Mappers between [LocalizedTextJson] (wire) and [LocalizedText] (domain).
 *
 * No Room entity counterpart: localized fields are denormalized into per-language
 * String columns on the entities themselves, so mappers for entities pull the
 * four columns directly into a [LocalizedText] without going through this type.
 */

internal fun LocalizedTextJson.toDomain(): LocalizedText = LocalizedText(
    en = en,
    ar = ar,
    zhSimplified = zhSimplified,
    esMx = esMx
)

internal fun LocalizedText.toJson(): LocalizedTextJson = LocalizedTextJson(
    en = en,
    ar = ar,
    zhSimplified = zhSimplified,
    esMx = esMx
)
