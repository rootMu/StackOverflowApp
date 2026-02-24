package com.example.stackoverflowapp.data.parser

import com.example.stackoverflowapp.data.api.BadgeCountsDto
import com.example.stackoverflowapp.data.api.UserDto
import com.example.stackoverflowapp.data.api.UsersResponseDto
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class JsonUsersResponseParser : UsersResponseParser {

    override fun parseUsersResponse(json: String): UsersResponseDto {
        return try {
            val root = JSONObject(json)
            val itemsArray = root.optJSONArray("items") ?: JSONArray()

            val users = buildList {
                for (index in 0 until itemsArray.length()) {
                    val item = itemsArray.optJSONObject(index) ?: continue
                    parseUser(item)?.let(::add)
                }
            }

            UsersResponseDto(items = users)
        } catch (e: JSONException) {
            throw JsonParsingException("Failed to parse users response JSON", e)
        } catch (e: Exception) {
            throw JsonParsingException("Unexpected error while parsing users response", e)
        }
    }

    private fun parseUser(json: JSONObject): UserDto? {
        val userId = json.optIntOrNull("user_id") ?: return null
        val displayName = json.optStringOrNull("display_name") ?: return null
        val reputation = json.optIntOrNull("reputation") ?: 0
        val profileImageUrl = json.optStringOrNull("profile_image")

        val badgeCounts = json.optJSONObject("badge_counts")?.let { badgesJson ->
            BadgeCountsDto(
                bronze = badgesJson.optIntOrDefault("bronze", 0),
                silver = badgesJson.optIntOrDefault("silver", 0),
                gold = badgesJson.optIntOrDefault("gold", 0)
            )
        }

        return UserDto(
            userId = userId,
            displayName = displayName,
            reputation = reputation,
            profileImageUrl = profileImageUrl,
            badgeCounts = badgeCounts
        )
    }
}

class JsonParsingException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

private fun JSONObject.optStringOrNull(key: String): String? {
    if (!has(key) || isNull(key)) return null
    return optString(key).takeIf { it.isNotBlank() }
}

private fun JSONObject.optIntOrNull(key: String): Int? {
    if (!has(key) || isNull(key)) return null
    return try {
        getInt(key)
    } catch (_: JSONException) {
        null
    }
}

private fun JSONObject.optIntOrDefault(key: String, default: Int): Int {
    return optIntOrNull(key) ?: default
}