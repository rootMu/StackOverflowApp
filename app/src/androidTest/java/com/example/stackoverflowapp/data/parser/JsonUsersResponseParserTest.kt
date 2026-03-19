package com.example.stackoverflowapp.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonUsersResponseParserTest {

    private val parser: UsersResponseParser = JsonUsersResponseParser()

    @Test
    fun parseUsersResponse_parsesSingleUserWithAllFields() {
        val json = """
            {
              "items": [
                {
                  "user_id": 22656,
                  "display_name": "Jon Skeet",
                  "reputation": 1454978,
                  "profile_image": "https://example.com/jon.png",
                  "location": "Reading, United Kingdom",
                  "website_url": "https://csharpindepth.com",
                  "badge_counts": {
                    "bronze": 9255,
                    "silver": 9202,
                    "gold": 877
                  }
                }
              ]
            }
        """.trimIndent()

        val result = parser.parseUsersResponse(json)

        assertEquals(1, result.items.size)
        val user = result.items.first()
        assertEquals(22656, user.userId)
        assertEquals("Jon Skeet", user.displayName)
        assertEquals(1454978, user.reputation)
        assertEquals("https://example.com/jon.png", user.profileImageUrl)
        assertEquals("Reading, United Kingdom", user.location)
        assertEquals("https://csharpindepth.com", user.websiteUrl)
        assertNotNull(user.badgeCounts)
        assertEquals(877, user.badgeCounts?.gold)
        assertEquals(9202, user.badgeCounts?.silver)
        assertEquals(9255, user.badgeCounts?.bronze)
    }

    @Test
    fun parseUsersResponse_parsesMultipleUsers() {
        val json = """
            {
              "items": [
                { "user_id": 1, "display_name": "User One", "reputation": 100 },
                { "user_id": 2, "display_name": "User Two", "reputation": 200 }
              ]
            }
        """.trimIndent()

        val result = parser.parseUsersResponse(json)

        assertEquals(2, result.items.size)
        assertEquals("User One", result.items[0].displayName)
        assertEquals("User Two", result.items[1].displayName)
    }

    @Test
    fun parseUsersResponse_skipsUsersMissingMandatoryFields() {
        val json = """
            {
              "items": [
                { "display_name": "Missing ID", "reputation": 100 },
                { "user_id": 2, "reputation": 200 },
                { "user_id": 3, "display_name": "Valid User", "reputation": 300 }
              ]
            }
        """.trimIndent()

        val result = parser.parseUsersResponse(json)

        assertEquals(1, result.items.size)
        assertEquals(3, result.items[0].userId)
        assertEquals("Valid User", result.items[0].displayName)
    }

    @Test
    fun parseUsersResponse_handlesNullAndEmptyOptionalFields() {
        val json = """
            {
              "items": [
                {
                  "user_id": 1,
                  "display_name": "Null User",
                  "reputation": 100,
                  "profile_image": null,
                  "location": "",
                  "website_url": "   "
                }
              ]
            }
        """.trimIndent()

        val result = parser.parseUsersResponse(json)

        val user = result.items.first()
        assertNull(user.profileImageUrl)
        assertNull(user.location)
        assertNull(user.websiteUrl)
    }

    @Test
    fun parseUsersResponse_handlesMissingBadgeCounts() {
        val json = """
            {
              "items": [
                { "user_id": 1, "display_name": "No Badges", "reputation": 10 }
              ]
            }
        """.trimIndent()

        val result = parser.parseUsersResponse(json)
        assertNull(result.items.first().badgeCounts)
    }

    @Test
    fun parseUsersResponse_handlesPartialBadgeCounts() {
        val json = """
            {
              "items": [
                {
                  "user_id": 1,
                  "display_name": "Partial Badges",
                  "reputation": 10,
                  "badge_counts": { "gold": 5 }
                }
              ]
            }
        """.trimIndent()

        val result = parser.parseUsersResponse(json)
        val badges = result.items.first().badgeCounts
        assertNotNull(badges)
        assertEquals(5, badges?.gold)
        assertEquals(0, badges?.silver)
        assertEquals(0, badges?.bronze)
    }

    @Test
    fun parseUsersResponse_handlesEmptyItemsArray() {
        val json = "{ \"items\": [] }"
        val result = parser.parseUsersResponse(json)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun parseUsersResponse_handlesMissingItemsArray() {
        val json = "{ \"quota_max\": 300 }"
        val result = parser.parseUsersResponse(json)
        assertTrue(result.items.isEmpty())
    }

    @Test(expected = JsonParsingException::class)
    fun parseUsersResponse_throwsExceptionForInvalidJson() {
        parser.parseUsersResponse("not a json")
    }

    @Test
    fun parseUsersResponse_handlesReputationDefault() {
        val json = """
            {
              "items": [
                { "user_id": 1, "display_name": "No Rep" }
              ]
            }
        """.trimIndent()

        val result = parser.parseUsersResponse(json)
        assertEquals(0, result.items.first().reputation)
    }
}
