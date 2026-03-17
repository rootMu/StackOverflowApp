package com.example.stackoverflowapp.ui.details

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.ui.components.AsyncImageWithCrossfade

/**
 * Shared transition duration constant.
 */
private const val TRANSITION_DURATION = 1000

/**
 * Implements the User Details screen with Shared Element transitions.
 *
 * @param user The user to display.
 * @param isFollowed Whether the user is followed.
 * @param onFollowClick Callback for the follow action.
 * @param imageLoader The loader for profile images.
 * @param sharedTransitionScope Scope for shared element animations.
 * @param animatedContentScope Scope for visibility animations within navigation.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun UserDetailsScreen(
    user: User,
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
    imageLoader: ImageLoader,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    with(sharedTransitionScope) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .sharedBounds(
                    rememberSharedContentState(key = "container_${user.id}"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = { _, _ -> tween(durationMillis = TRANSITION_DURATION) }
                )
                .graphicsLayer { rotationZ = 0f }
                .background(Color(0xFFFFFEFB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFFFFEFB))
                    .padding(16.dp)
            ) {
                AsyncImageWithCrossfade(
                    url = user.profileImageUrl,
                    imageLoader = imageLoader,
                    displayName = user.displayName,
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedElement(
                            rememberSharedContentState(key = "image_${user.id}"),
                            animatedVisibilityScope = animatedContentScope,
                            boundsTransform = { _, _ -> tween(durationMillis = TRANSITION_DURATION) }
                        )
                )
            }

            Column(Modifier.padding(24.dp)) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic
                )

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                // Stats Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFF2B705),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                user.reputation.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "Reputation",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Metadata Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        user.location ?: "Unknown Location",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                val website = user.websiteUrl
                if (!website.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = website,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { uriHandler.openUri(website) }
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Follow Action
                FilledTonalButton(
                    onClick = onFollowClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isFollowed) Color(0xFFF0E1B8) else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isFollowed) "Following" else "Follow User",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isFollowed) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = "follow_star_${user.id}"),
                                    animatedVisibilityScope = animatedContentScope,
                                    boundsTransform = { _, _ -> tween(durationMillis = TRANSITION_DURATION) }
                                ),
                            tint = if (isFollowed) Color(0xFF6F530A) else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
