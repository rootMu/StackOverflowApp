package com.example.stackoverflowapp.data.parser

import org.junit.Assert
import org.junit.Test

class JsonUsersResponseParserTest {

    private val parser: UsersResponseParser = JsonUsersResponseParser()

    @Test
    fun parseUsersResponse_parsesSingleUser() {
        val json = """
            {
              "items": [
                {
                  "user_id": 22656,
                  "display_name": "Jon Skeet",
                  "reputation": 1454978,
                  "profile_image": "https://example.com/jon.png",
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

        Assert.assertEquals(1, result.items.size)
        val user = result.items.first()
        Assert.assertEquals(22656, user.userId)
        Assert.assertEquals("Jon Skeet", user.displayName)
        Assert.assertEquals(1454978, user.reputation)
        Assert.assertEquals("https://example.com/jon.png", user.profileImageUrl)
        Assert.assertEquals(877, user.badgeCounts?.gold)
    }

    @Test
    fun parseUsersResponse_handlesNullOptionalFields() {
        val json = """
            {
              "items": [
                {
                  "user_id": 1,
                  "display_name": "No Image User",
                  "reputation": 100,
                  "profile_image": null
                }
              ]
            }
        """.trimIndent()

        val result = parser.parseUsersResponse(json)

        Assert.assertEquals(1, result.items.size)
        Assert.assertNull(result.items.first().profileImageUrl)
    }

    @Test
    fun parserUsersResponse_handlesJSONMissingItemsArray() {
        val malformedJson = "{ \"quota_max\": 300 }"
        val parser = JsonUsersResponseParser()

        val result = parser.parseUsersResponse(malformedJson)
        Assert.assertTrue(result.items.isEmpty())
    }

    @Test(expected = JsonParsingException::class)
    fun parseUsersResponse_throwsControlledException_forInvalidJson() {
        parser.parseUsersResponse("{ invalid json")
    }
}