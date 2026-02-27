import com.example.stackoverflowapp.data.api.ApiResult
import com.example.stackoverflowapp.data.api.FakeStackOverflowUsersApi
import com.example.stackoverflowapp.data.api.UserDto
import com.example.stackoverflowapp.data.api.UsersResponseDto
import com.example.stackoverflowapp.data.repo.UserRepositoryImpl
import com.example.stackoverflowapp.data.storage.FakeUserDatabase
import com.example.stackoverflowapp.domain.model.User
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

    private fun createUser(id: Int) =
        User(id, "User $id", 100, null)

    private fun setupRepository(apiResult: ApiResult<UsersResponseDto>) {
        fakeApi = FakeStackOverflowUsersApi(apiResult)
        repository = UserRepositoryImpl(fakeApi, fakeDb)
    }

    private fun setApiSuccess(users: List<User>) {
        setupRepository(
            ApiResult.Success(
                users.toDto()
            )
        )
        fakeApi.setResponse(ApiResult.Success(users.toDto()))
    }

    private fun List<User>.toDto() = UsersResponseDto(items = map { it.toDto() })

    private fun User.toDto() = UserDto(
        userId = id,
        displayName = displayName,
        reputation = reputation,
        profileImageUrl = profileImageUrl
    )
    // --- Tests ---

    @Test
    fun `fetchTopUsers returns local data when available`() = runTest {
        val user = createUser(1)
        val list = listOf(user)
        fakeDb.insertUsers(list)

        setupRepository(
            ApiResult.Success(
                list.toDto()
            )
        )

        val result = repository.fetchTopUsers()

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(user, result.getOrThrow().first())
        Assert.assertEquals(0, fakeApi.callCount)
    }

    @Test
    fun `error branches return correct failure messages`() = runTest {
        val errorCases = listOf(
            ApiResult.Error.Http(404, "Not Found") to "HTTP 404: Not Found",
            ApiResult.Error.Http(500, null) to "HTTP 500: Request failed",
            ApiResult.Error.EmptyBody to "Empty response body",
            ApiResult.Error.Network("No Internet") to "No Internet",
            ApiResult.Error.Parse("Malformed JSON") to "Malformed JSON"
        )

        errorCases.forEach { (apiError, expectedMessage) ->
            setupRepository(apiError)

            val result = repository.fetchTopUsers()

            Assert.assertTrue("Expected failure for $apiError", result.isFailure)
            Assert.assertEquals(expectedMessage, result.exceptionOrNull()?.message)
        }
    }

    @Test
    fun `refreshUsers replaces local cache with fresh data`() = runTest {
        fakeDb.insertUsers(listOf(createUser(1)))
        val freshUser = createUser(2)
        val list = listOf(freshUser)
        setApiSuccess(list)

        setupRepository(
            ApiResult.Success(
                list.toDto()
            )
        )

        repository.refreshUsers()

        Assert.assertEquals(freshUser, fakeDb.getAllUsers().first())
        Assert.assertEquals(1, fakeDb.getAllUsers().size)
    }
}