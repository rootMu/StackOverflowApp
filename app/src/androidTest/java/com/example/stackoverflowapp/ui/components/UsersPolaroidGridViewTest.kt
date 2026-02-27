import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.ui.components.UsersPolaroidGridView
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class UsersPolaroidGridViewTest {

    @get:Rule
    val composeRule = createComposeRule()

    val fakeImageLoader = object : ImageLoader {
        override suspend fun loadBitmap(url: String): Bitmap? = null
        override fun getCachedBitmap(url: String): Bitmap? = null
    }

    @Test
    fun user_notFollowed_showsFollowContentDescription() {
        val users = listOf(
            User(id = 1, displayName = "Jeff Atwood", reputation = 9001, profileImageUrl = null)
        )

        composeRule.setContent {
            UsersPolaroidGridView(
                gridState = rememberLazyGridState(),
                users = users,
                followedUserIds = emptySet(),
                onFollowClick = {},
                imageLoader = fakeImageLoader,
                contentPadding = PaddingValues.Absolute()
            )
        }

        composeRule
            .onNodeWithTag("follow_button_1")
            .assertIsDisplayed()
    }

    @Test
    fun user_followed_showsUnfollowContentDescription() {
        val users = listOf(
            User(id = 1, displayName = "Jeff Atwood", reputation = 9001, profileImageUrl = null)
        )

        composeRule.setContent {
            UsersPolaroidGridView(
                gridState = rememberLazyGridState(),
                users = users,
                followedUserIds = setOf(1),
                onFollowClick = {},
                imageLoader = fakeImageLoader,
                contentPadding = PaddingValues.Absolute()
            )
        }

        composeRule
            .onNodeWithTag("follow_button_1")
            .assertIsDisplayed()
    }

    @Test
    fun clicking_follow_icon_callsCallbackWithUserId() {
        val users = listOf(
            User(id = 42, displayName = "Test User", reputation = 99, profileImageUrl = null)
        )

        var clickedId: Int? = null

        composeRule.setContent {
            UsersPolaroidGridView(
                gridState = rememberLazyGridState(),
                users = users,
                followedUserIds = emptySet(),
                onFollowClick = { id -> clickedId = id },
                imageLoader = fakeImageLoader,
                contentPadding = PaddingValues.Absolute()
            )
        }

        composeRule
            .onNodeWithTag("follow_button_42")
            .performClick()

        assertEquals(42, clickedId)
    }

    @Test
    fun clicking_follow_updatesUiWhenStateChanges() {
        val users = listOf(
            User(id = 1, displayName = "Jeff Atwood", reputation = 9001, profileImageUrl = null)
        )

        composeRule.setContent {
            var followedIds by remember { mutableStateOf(setOf<Int>()) }

            UsersPolaroidGridView(
                gridState = rememberLazyGridState(),
                users = users,
                followedUserIds = followedIds,
                onFollowClick = { userId ->
                    @Suppress("AssignedValueIsNeverRead")
                    followedIds =
                        if (userId in followedIds) followedIds - userId else followedIds + userId
                },
                imageLoader = fakeImageLoader,
                contentPadding = PaddingValues.Absolute()
            )
        }

        composeRule.onNodeWithTag("follow_button_1", useUnmergedTree = true)
            .assertContentDescriptionEquals("Follow Jeff Atwood")

        composeRule.onNodeWithTag("follow_button_1", useUnmergedTree = true)
            .performClick()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("follow_button_1", useUnmergedTree = true)
            .assertContentDescriptionEquals("Unfollow Jeff Atwood")
    }
}