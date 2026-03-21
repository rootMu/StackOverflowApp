package com.example.stackoverflowapp.ui.components

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.User
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlin.math.abs

/**
 * Shared transition duration constant.
 */
private const val TRANSITION_DURATION = 1000

/**
 * A grid view of users displayed as Polaroid-style cards.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun UsersPolaroidGridView(
    gridState: LazyGridState,
    users: List<User>,
    followedUsers: Set<Int>,
    onUserClick: (Int) -> Unit,
    onFollowClick: (Int) -> Unit,
    imageLoader: ImageLoader,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    isLoadingMore: Boolean = false,
    isEndReached: Boolean = false,
    onLoadMore: () -> Unit = {}
) {
    val shouldLoadMore = remember(isLoadingMore, isEndReached) {
        derivedStateOf {
            if (isLoadingMore || isEndReached) return@derivedStateOf false
            
            val layoutInfo = gridState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo
            
            if (totalItemsNumber <= 0 || visibleItems.isEmpty()) {
                false
            } else {
                val lastVisibleItemIndex = visibleItems.last().index + 1
                lastVisibleItemIndex >= (totalItemsNumber - 4) && totalItemsNumber > visibleItems.size
            }
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                onLoadMore()
            }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .testTag("users_grid"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(users, key = { it.id }) { user ->
            UserPolaroidCard(
                user = user,
                isFollowed = user.id in followedUsers,
                onUserClick = { onUserClick(user.id) },
                onFollowClick = { onFollowClick(user.id) },
                imageLoader = imageLoader,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                modifier = Modifier.animateItem()
            )
        }

        if (isLoadingMore) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("pagination_loading_indicator")
                    )
                }
            }
        }
    }
}

/**
 * A Polaroid-style card representing a user.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun UserPolaroidCard(
    user: User,
    isFollowed: Boolean,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit,
    imageLoader: ImageLoader,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier
) {
    val tiltDegrees = remember(user.id) { tiltForUser(user.id) }
    val reputationText = remember(user.reputation) { formatReputation(user.reputation) }

    with(sharedTransitionScope) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .sharedBounds(
                    rememberSharedContentState(key = "container_${user.id}"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = { _, _ -> tween(durationMillis = TRANSITION_DURATION) }
                )
                .clickable { onUserClick() }
                .graphicsLayer { rotationZ = tiltDegrees },
            shape = RoundedCornerShape(5.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEFB))
        ) {
            Column(Modifier.padding(start = 6.dp, top = 6.dp, end = 6.dp, bottom = 10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFEDEFFF))
                ) {
                    AsyncImageWithCrossfade(
                        url = user.profileImageUrl,
                        imageLoader = imageLoader,
                        displayName = user.displayName,
                        modifier = Modifier
                            .matchParentSize()
                            .sharedElement(
                                rememberSharedContentState(key = "image_${user.id}"),
                                animatedVisibilityScope = animatedContentScope,
                                boundsTransform = { _, _ -> tween(durationMillis = TRANSITION_DURATION) }
                            )
                    )

                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(reputationText, fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = Color(0xFFF0E1B8),
                            disabledLabelColor = Color(0xFF6F530A)
                        ),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    )

                    IconButton(
                        onClick = onFollowClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = if (isFollowed) {
                                    "Unfollow ${user.displayName}"
                                } else {
                                    "Follow ${user.displayName}"
                                }
                            }
                            .testTag("follow_button_${user.id}")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xCCFFFFFF),
                            shadowElevation = 2.dp
                        ) {
                            Icon(
                                imageVector = if (isFollowed) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                tint = if (isFollowed) Color(0xFFF2B705) else Color(0xFF555555),
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = user.displayName,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF1F1F1F),
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

/**
 * Asynchronously loads an image and displays it with a crossfade effect.
 */
@Composable
fun AsyncImageWithCrossfade(
    url: String?,
    imageLoader: ImageLoader,
    displayName: String,
    modifier: Modifier = Modifier
) {
    var displayBitmap by remember(url) { mutableStateOf(url?.let { imageLoader.getCachedBitmap(it) }) }

    LaunchedEffect(url) {
        if (url != null && displayBitmap == null) {
            displayBitmap = imageLoader.loadBitmap(url)
        }
    }

    if (displayBitmap != null) {
        Image(
            bitmap = displayBitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        PolaroidPlaceholderPhoto(displayName, modifier)
    }
}

@Composable
private fun PolaroidPlaceholderPhoto(displayName: String, modifier: Modifier = Modifier) {
    Box(modifier.background(Color(0xFFEDEFFF)), contentAlignment = Alignment.Center) {
        Surface(shape = CircleShape, color = Color(0xFFD9DFFF), modifier = Modifier.size(52.dp)) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "$displayName placeholder",
                tint = Color(0xFF3B4260),
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

private fun tiltForUser(id: Int): Float =
    ((abs(id * 1103515245 + 12345) % 500).toFloat() / 100f) - 2.5f

private fun formatReputation(reputation: Int): String = when {
    reputation >= 1_000_000 -> "%.1fM".format(reputation / 1_000_000f).replace(".0", "")
    reputation >= 1_000 -> "%.1fk".format(reputation / 1_000f).replace(".0", "")
    else -> reputation.toString()
}
