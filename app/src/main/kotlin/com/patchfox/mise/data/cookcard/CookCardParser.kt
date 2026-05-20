package com.patchfox.mise.data.cookcard

import com.patchfox.mise.data.dto.CookCardDto
import kotlinx.serialization.json.Json

object CookCardParser {

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
        encodeDefaults = true
    }

    fun parse(jsonPayload: String): CookCardDto = json.decodeFromString(CookCardDto.serializer(), jsonPayload)
}
