package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.data.api.ApiResult
import com.example.stackoverflowapp.data.api.BadgeCountsDto
import com.example.stackoverflowapp.data.api.StackOverflowUsersApi
import com.example.stackoverflowapp.data.api.UserDto
import com.example.stackoverflowapp.data.api.UsersResponseDto
import com.example.stackoverflowapp.domain.model.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRepositoryImplTest {

    @Test
    fun `fetchTopUsers() returns mapped domain users when Api Succeeds`() = runTest {
        assertRepoSuccess(
            apiResult = ApiResult.Success(
                UsersResponseDto(
                    items = listOf(
                        userDto(
                            userId = 22656,
                            displayName = "Jon Skeet",
                            reputation = 1_454_978,
                            profileImageUrl = "https://example.com/jon.png",
                            badgeCounts = BadgeCountsDto(
                                bronze = 9255,
                                silver = 9202,
                                gold = 877
                            )
                        ),
                        userDto(
                            userId = 1,
                            displayName = "Jeff Atwood",
                            reputation = 9001,
                            profileImageUrl = null
                        )
                    )
                )
            )
        ) { users ->
            assertEquals(2, users.size)

            assertUser(
                actual = users[0],
                expectedId = 22656,
                expectedName = "Jon Skeet",
                expectedReputation = 1_454_978,
                expectedProfileImageUrl = "https://example.com/jon.png"
            )

            assertUser(
                actual = users[1],
                expectedId = 1,
                expectedName = "Jeff Atwood",
                expectedReputation = 9001,
                expectedProfileImageUrl = null
            )
        }
    }

    @Test
    fun `fetchTopUsers() returns failure when Api returns http error`() = runTest {
        assertRepoFailure(
            apiResult = ApiResult.Error.Http(code = 404, message = "Not Found"),
            expectedMessagePart = "404"
        )
    }

    @Test
    fun `fetchTopUsers() returns failure when Api returns empty body`() = runTest {
        assertRepoFailure(
            apiResult = ApiResult.Error.EmptyBody
        )
    }

    @Test
    fun `fetchTopUsers() returns empty list when Api returns empty items`() = runTest {
        assertRepoSuccess(
            apiResult = ApiResult.Success(UsersResponseDto(items = emptyList()))
        ) { users ->
            assertTrue(users.isEmpty())
        }
    }

    @Test
    fun `fetchTopUsers() returns failure when Api returns network error`() = runTest {
        assertRepoFailure(
            apiResult = ApiResult.Error.Network(message = "timeout"),
            expectedMessagePart = "timeout"
        )
    }

    @Test
    fun `fetchTopUsers() returns failure when Api returns parse error`() = runTest {
        assertRepoFailure(
            apiResult = ApiResult.Error.Parse(message = "Malformed JSON"),
            expectedMessagePart = "Malformed JSON"
        )
    }

    @Test
    fun `fetchTopUsers() calls Api with default page and page size`() = runTest {
        val api = FakeStackOverflowUsersApi(
            result = ApiResult.Success(UsersResponseDto(items = emptyList()))
        )
        val repository = UserRepositoryImpl(api)

        repository.fetchTopUsers()

        assertEquals(1, api.callCount)
        assertEquals(1, api.lastPage)
        assertEquals(20, api.lastPageSize)
    }

    private suspend fun fetchFromRepo(
        apiResult: ApiResult<UsersResponseDto>
    ): Result<List<User>> {
        val api = FakeStackOverflowUsersApi(result = apiResult)
        val repository = UserRepositoryImpl(api)
        return repository.fetchTopUsers()
    }

    private suspend fun assertRepoSuccess(
        apiResult: ApiResult<UsersResponseDto>,
        assertion: (List<User>) -> Unit
    ) {
        val result = fetchFromRepo(apiResult)
        assertTrue(result.isSuccess)
        assertion(result.getOrThrow())
    }

    private suspend fun assertRepoFailure(
        apiResult: ApiResult<UsersResponseDto>,
        expectedMessagePart: String? = null
    ) {
        val result = fetchFromRepo(apiResult)
        assertTrue(result.isFailure)

        if (expectedMessagePart != null) {
            assertTrue(result.exceptionOrNull()?.message.orEmpty().contains(expectedMessagePart))
        }
    }

    private fun userDto(
        userId: Int,
        displayName: String,
        reputation: Int,
        profileImageUrl: String?,
        badgeCounts: BadgeCountsDto? = null
    ): UserDto {
        return UserDto(
            userId = userId,
            displayName = displayName,
            reputation = reputation,
            profileImageUrl = profileImageUrl,
            badgeCounts = badgeCounts
        )
    }

    private fun assertUser(
        actual: User,
        expectedId: Int,
        expectedName: String,
        expectedReputation: Int,
        expectedProfileImageUrl: String?
    ) {
        assertEquals(expectedId, actual.id)
        assertEquals(expectedName, actual.displayName)
        assertEquals(expectedReputation, actual.reputation)
        assertEquals(expectedProfileImageUrl, actual.profileImageUrl)
    }

    private class FakeStackOverflowUsersApi(
        private val result: ApiResult<UsersResponseDto>
    ) : StackOverflowUsersApi {

        var callCount: Int = 0
            private set

        var lastPage: Int? = null
            private set

        var lastPageSize: Int? = null
            private set

        override suspend fun fetchTopUsers(
            page: Int,
            pageSize: Int
        ): ApiResult<UsersResponseDto> {
            callCount++
            lastPage = page
            lastPageSize = pageSize
            return result
        }
    }
}