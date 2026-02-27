package com.example.stackoverflowapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.User
import kotlin.math.abs

@Composable
fun UsersPolaroidGridView(
    gridState: LazyGridState,
    users: List<User>,
    followedUserIds: Set<Int>,
    onFollowClick: (Int) -> Unit,
    imageLoader: ImageLoader,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
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
                isFollowed = user.id in followedUserIds,
                onFollowClick = { onFollowClick(user.id) },
                imageLoader = imageLoader
            )
        }
    }
}

@Composable
private fun UserPolaroidCard(
    user: User,
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
    imageLoader: ImageLoader
) {
    val tiltDegrees = remember(user.id) { tiltForUser(user.id) }
    val reputationText = remember(user.reputation) { formatReputation(user.reputation) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                    modifier = Modifier.matchParentSize()
                )

                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            reputationText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    },
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
                        .testTag("follow_button_${user.id}")
                        .clearAndSetSemantics {
                            contentDescription =
                                if (isFollowed) "Unfollow ${user.displayName}" else "Follow ${user.displayName}"
                        }
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