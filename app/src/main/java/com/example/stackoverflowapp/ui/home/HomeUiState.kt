package com.example.stackoverflowapp.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.stackoverflowapp.domain.model.User

/**
 * Represents the full state of the Home screen.
 */
data class HomeScreenState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val users: List<UserUiModel> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.Default,
    val showFavouritesOnly: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false
)

/**
 * UI-specific wrapper for the [User] domain model.
 */
data class UserUiModel(
    val user: User,
    val isFollowed: Boolean
)

/**
 * Available fields for sorting users.
 * 
 * @property displayName User-friendly string for UI display.
 */
enum class SortField(val displayName: String) {
    NAME("Name"),
    REPUTATION("Popularity"),
    CREATION("Creation"),
    MODIFIED("Modified")
}

/**
 * Represents sorting direction with associated UI metadata.
 * 
 * @property icon The [ImageVector] representing this direction.
 * @property contentDescription Accessibility label for this direction.
 */
enum class SortDirection(
    val icon: ImageVector,
    val contentDescription: String
) {
    ASC(Icons.Default.ArrowUpward, "Sort Ascending"),
    DESC(Icons.Default.ArrowDownward, "Sort Descending")
}

/**
 * Combined model for sorting configuration.
 */
data class SortOrder(
    val field: SortField,
    val direction: SortDirection
) {
    companion object {
        /** Default sorting: highest reputation first. */
        val Default = SortOrder(
            field = SortField.REPUTATION,
            direction = SortDirection.DESC
        )
    }

    /**
     * Returns a new [SortOrder] with the opposite direction.
     */
    fun toggleDirection(): SortOrder =
        copy(
            direction = when (direction) {
                SortDirection.ASC -> SortDirection.DESC
                SortDirection.DESC -> SortDirection.ASC
            }
        )
}

/**
 * Sealed interface representing the internal load state of the user list.
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val users: List<User>,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val currentPage: Int = 1,
        val endReached: Boolean = false
    ) : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/**
 * Helper to convert a list of users to the appropriate list state.
 */
internal fun List<User>.toHomeUiState(): HomeUiState =
    if (isEmpty()) {
        HomeUiState.Empty
    } else {
        HomeUiState.Success(
            users = this,
            isRefreshing = false
        )
    }
