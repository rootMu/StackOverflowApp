package com.example.stackoverflowapp.ui.details

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.ui.components.AsyncImageWithCrossfade
import com.example.stackoverflowapp.ui.transitions.LocalAnimatedVisibilityScope
import com.example.stackoverflowapp.ui.transitions.LocalSharedTransitionScope

/**
 * Duration for shared element transitions.
 */
private const val TRANSITION_DURATION = 1000

/**
 * UserDetailsScreen displays the full profile of a StackOverflow user.
 * It features a pinned image header that content scrolls over,
 * and a sticky follow button at the bottom.
 *
 * Uses [LocalSharedTransitionScope] and [LocalAnimatedVisibilityScope]
 * to avoid prop-drilling animation scopes.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun UserDetailsScreen(
    user: User,
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    val scrollOffset = scrollState.value.toFloat()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val imageHeight = maxWidth

        PinnedHeader(
            user = user,
            imageHeight = imageHeight,
            scrollOffset = scrollOffset,
            imageLoader = imageLoader
        )

        Content(
            user = user,
            scrollState = scrollState,
            imageHeight = imageHeight,
            uriHandler = uriHandler
        )

        StickyFollowButton(
            isFollowed = isFollowed,
            onFollowClick = onFollowClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun Content(user: User, scrollState: ScrollState, imageHeight: Dp, uriHandler: UriHandler) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(imageHeight))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(24.dp)
        ) {
            UserInfoHeader(user)

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            ReputationSection(user)

            Spacer(Modifier.height(24.dp))

            InfoRow(icon = Icons.Filled.LocationOn, text = user.location ?: "Unknown Location")

            user.websiteUrl?.let { website ->
                if (website.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    InfoRow(
                        icon = Icons.Filled.Language,
                        text = website,
                        modifier = Modifier.clickable { uriHandler.openUri(website) },
                        textColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (!user.aboutMe.isNullOrBlank()) {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "About Me",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                HtmlText(
                    html = user.aboutMe,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_bio")
                )
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

/**
 * Header that remains fixed with a fade effect as content scrolls over it.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PinnedHeader(
    user: User,
    imageHeight: Dp,
    scrollOffset: Float,
    imageLoader: ImageLoader
) {
    val sharedScope = LocalSharedTransitionScope.current
    val visibilityScope = LocalAnimatedVisibilityScope.current

    if (sharedScope != null && visibilityScope != null) {
        with(sharedScope) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .padding(16.dp)
                    .sharedBounds(
                        rememberSharedContentState(key = "container_${user.id}"),
                        animatedVisibilityScope = visibilityScope,
                        boundsTransform = { _, _ -> tween(durationMillis = TRANSITION_DURATION) }
                    )
                    .graphicsLayer {
                        alpha = (1f - (scrollOffset / imageHeight.toPx())).coerceIn(0f, 1f)
                    }
                    .background(Color(0xFFFFFEFB), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImageWithCrossfade(
                    url = user.profileImageUrl,
                    imageLoader = imageLoader,
                    displayName = user.displayName,
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedElement(
                            rememberSharedContentState(key = "image_${user.id}"),
                            animatedVisibilityScope = visibilityScope,
                            boundsTransform = { _, _ -> tween(durationMillis = TRANSITION_DURATION) }
                        )
                )
            }
        }
    }
}

@Composable
private fun UserInfoHeader(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.weight(1f)
        )

        user.badgeCounts?.let { counts ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgeItem(count = counts.gold, color = Color(0xFFFFD700))
                BadgeItem(count = counts.silver, color = Color(0xFFC0C0C0))
                BadgeItem(count = counts.bronze, color = Color(0xFFCD7F32))
            }
        }
    }
}

@Composable
private fun ReputationSection(user: User) {
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
                text = user.reputation.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "Reputation",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

/**
 * Sticky bottom button
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun StickyFollowButton(
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
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
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isFollowed) Color(0xFF6F530A) else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isFollowed) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isFollowed) Color(0xFF6F530A) else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Renders HTML content using a native TextView for accurate StackOverflow formatting.
 */
@Composable
private fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val fontSize = MaterialTheme.typography.bodyMedium.fontSize.value

    AndroidView(
        modifier = modifier.semantics {
            val plainText = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
            this.text = AnnotatedString(plainText)
        },
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextColor(textColor.toArgb())
                textSize = fontSize
                setTextIsSelectable(true)
            }
        },
        update = {
            it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }
    )
}

@Composable
private fun BadgeItem(count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), CircleShape)
            .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}
