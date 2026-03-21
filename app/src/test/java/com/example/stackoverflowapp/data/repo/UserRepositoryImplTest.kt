package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.data.api.ApiResult
import com.example.stackoverflowapp.data.api.BadgeCountsDto
import com.example.stackoverflowapp.data.api.FakeStackOverflowUsersApi
import com.example.stackoverflowapp.data.api.UserDto
import com.example.stackoverflowapp.data.api.UsersResponseDto
import com.example.stackoverflowapp.data.storage.FakeUserDatabase
import com.example.stackoverflowapp.domain.model.AppError
import com.example.stackoverflowapp.domain.model.AppErrorException
import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.domain.model.createTestUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private lateinit var fakeApi: FakeStackOverflowUsersApi
    private lateinit var fakeDb: FakeUserDatabase
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setup() {
        fakeDb = FakeUserDatabase()
    }

    private fun setupRepository(apiResult: ApiResult<UsersResponseDto>) {
        fakeApi = FakeStackOverflowUsersApi(apiResult)
        repository = UserRepositoryImpl(fakeApi, fakeDb)
    }

    private fun setApiSuccess(users: List<User>) {
        setupRepository(ApiResult.Success(users.toDto()))
        fakeApi.setResponse(ApiResult.Success(users.toDto()))
    }

    private fun List<User>.toDto() = UsersResponseDto(items = map { it.toDto() })

    private fun User.toDto() = UserDto(
        userId = id,
        displayName = displayName,
        reputation = reputation,
        profileImageUrl = profileImageUrl,
        badgeCounts = badgeCounts?.let {
            BadgeCountsDto(
                bronze = it.bronze,
                silver = it.silver,
                gold = it.gold
            )
        },
        location = location,
        websiteUrl = websiteUrl,
        aboutMe = aboutMe
    )

    @Test
    fun `fetchTopUsers returns local data when available on page 1`() = runTest {
        val user = createTestUser(1)
        val list = listOf(user)
        fakeDb.insertUsers(list)

        setupRepository(ApiResult.Success(list.toDto()))

        val result = repository.fetchTopUsers(page = 1)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(user, result.getOrThrow().first())
        Assert.assertEquals(0, fakeApi.callCount)
    }

    @Test
    fun `fetchTopUsers page 2 always calls API even if local data exists`() = runTest {
        val user = createTestUser(1)
        fakeDb.insertUsers(listOf(user))

        val apiUser = createTestUser(2)
        setupRepository(ApiResult.Success(listOf(apiUser).toDto()))

        val result = repository.fetchTopUsers(page = 2)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(apiUser, result.getOrThrow().first())
        Assert.assertEquals(1, fakeApi.callCount)
    }

    @Test
    fun `error branches return correct AppError types`() = runTest {
        val errorCases = listOf(
            ApiResult.Error.Http(401, "Unauthorized") to AppError.Network.Unauthorized,
            ApiResult.Error.Http(500, null) to AppError.Network.ServerError,
            ApiResult.Error.EmptyBody to AppError.Data.NotFound,
            ApiResult.Error.Network("No Internet") to AppError.Network.NoConnection,
            ApiResult.Error.Parse("Malformed JSON") to AppError.Data.MalformedResponse
        )

        errorCases.forEach { (apiError, expectedAppError) ->
            setupRepository(apiError)

            val result = repository.fetchTopUsers()

            Assert.assertTrue("Expected failure for $apiError", result.isFailure)
            val exception = result.exceptionOrNull()
            Assert.assertTrue("Expected AppErrorException but got $exception", exception is AppErrorException)
            Assert.assertEquals(expectedAppError, (exception as AppErrorException).error)
        }
    }

    @Test
    fun `refreshUsers replaces local cache with fresh data`() = runTest {
        fakeDb.insertUsers(listOf(createTestUser(1)))
        val freshUser = createTestUser(2)
        val list = listOf(freshUser)
        setApiSuccess(list)

        repository.refreshUsers()

        Assert.assertEquals(freshUser, fakeDb.getAllUsers().first())
        Assert.assertEquals(1, fakeDb.getAllUsers().size)
    }

    @Test
    fun `fetchUserDetails returns local data if aboutMe is present`() = runTest {
        val user = createTestUser(id = 123, aboutMe = "I have a bio")
        fakeDb.insertUsers(listOf(user))
        setupRepository(ApiResult.Success(UsersResponseDto(emptyList())))

        val result = repository.fetchUserDetails(123)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(user, result.getOrThrow())
        Assert.assertEquals(0, fakeApi.callCount)
    }

    @Test
    fun `fetchUserDetails calls API and updates cache if aboutMe is missing locally`() = runTest {
        val localUser = createTestUser(id = 123, aboutMe = null)
        val apiUser = createTestUser(id = 123, aboutMe = "Bio from API")

        fakeDb.insertUsers(listOf(localUser))
        setupRepository(ApiResult.Success(UsersResponseDto(listOf(apiUser.toDto()))))

        val result = repository.fetchUserDetails(123)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(apiUser, result.getOrThrow())
        Assert.assertEquals(1, fakeApi.callCount)
        Assert.assertEquals("Bio from API", fakeDb.getUserById(123)?.aboutMe)
    }

    @Test
    fun `fetchUserDetails falls back to local data if API fails and user exists`() = runTest {
        val localUser = createTestUser(id = 123, aboutMe = null)
        fakeDb.insertUsers(listOf(localUser))
        setupRepository(ApiResult.Error.Network("No Internet"))

        val result = repository.fetchUserDetails(123)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(localUser, result.getOrThrow())
    }

    @Test
    fun `fetchUserDetails returns failure if API fails and user not in database`() = runTest {
        setupRepository(ApiResult.Error.Network("No Internet"))

        val result = repository.fetchUserDetails(999)

        Assert.assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as AppErrorException
        Assert.assertEquals(AppError.Network.NoConnection, exception.error)
    }

    @Test
    fun `fetchUserDetails returns failure when API succeeds but items list is empty`() = runTest {
        setupRepository(ApiResult.Success(UsersResponseDto(emptyList())))

        val result = repository.fetchUserDetails(123)

        Assert.assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as AppErrorException
        Assert.assertEquals(AppError.Data.NotFound, exception.error)
    }

    @Test
    fun `fetchUserDetails overwrites stale local user with richer API user`() = runTest {
        val localUser = createTestUser(id = 123, aboutMe = null, location = "Old Location")
        val apiUser = createTestUser(id = 123, aboutMe = "Rich Bio", location = "New Location")

        fakeDb.insertUsers(listOf(localUser))
        setupRepository(ApiResult.Success(UsersResponseDto(listOf(apiUser.toDto()))))

        val result = repository.fetchUserDetails(123)

        Assert.assertEquals(apiUser, result.getOrThrow())
        val storedUser = fakeDb.getUserById(123)
        Assert.assertEquals("Rich Bio", storedUser?.aboutMe)
        Assert.assertEquals("New Location", storedUser?.location)
    }

    @Test
    fun `fetchUserDetails prefers local complete user even if API would return different data`() =
        runTest {
            val localUser =
                createTestUser(id = 123, aboutMe = "Existing Bio", location = "Location A")
            val apiUser =
                createTestUser(id = 123, aboutMe = "Different Bio", location = "Location B")

            fakeDb.insertUsers(listOf(localUser))
            setupRepository(ApiResult.Success(UsersResponseDto(listOf(apiUser.toDto()))))

            val result = repository.fetchUserDetails(123)

            Assert.assertEquals(localUser, result.getOrThrow())
            Assert.assertEquals(0, fakeApi.callCount)
        }

    @Test
    fun `fetchTopUsers returns API users in original order after caching`() = runTest {
        val users = listOf(
            createTestUser(id = 1, reputation = 100),
            createTestUser(id = 2, reputation = 200),
            createTestUser(id = 3, reputation = 150)
        )
        setupRepository(ApiResult.Success(users.toDto()))

        val result = repository.fetchTopUsers()

        Assert.assertEquals(users, result.getOrThrow())
    }
}
