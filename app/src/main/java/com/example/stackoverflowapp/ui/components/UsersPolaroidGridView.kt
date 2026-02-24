package com.example.stackoverflowapp.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
    users: List<User>,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = users,
            key = { user -> user.id }
        ) { user ->
            CompactPolaroidUserCard(user = user, imageLoader = imageLoader)
        }
    }
}

@Composable
private fun CompactPolaroidUserCard(
    user: User,
    imageLoader: ImageLoader
) {
    val tiltDegrees = tiltForUser(user.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .rotate(tiltDegrees),
        shape = RoundedCornerShape(5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEFB))
    ) {
        Column(
            modifier = Modifier.padding(start = 6.dp, top = 6.dp, end = 6.dp, bottom = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFEDEFFF))
            ) {
                UserPhotoOrPlaceholder(
                    imageUrl = user.profileImageUrl,
                    displayName = user.displayName,
                    imageLoader = imageLoader,
                    modifier = Modifier.matchParentSize()
                )

                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = formatReputation(user.reputation),
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
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 2.dp)
            ) {
                Text(
                    text = user.displayName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF1F1F1F),
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun UserPhotoOrPlaceholder(
    imageUrl: String?,
    displayName: String,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    val bitmapState by produceState<Bitmap?>(initialValue = null, imageUrl) {
        value = if (imageUrl.isNullOrBlank()) {
            null
        } else {
            imageLoader.loadBitmap(imageUrl)
        }
    }

    if (bitmapState != null) {
        Image(
            bitmap = bitmapState!!.asImageBitmap(),
            contentDescription = "$displayName profile image",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        PolaroidPlaceholderPhoto(
            displayName = displayName,
            modifier = modifier
        )
    }
}

@Composable
private fun PolaroidPlaceholderPhoto(
    displayName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFFEDEFFF)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFD9DFFF),
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "$displayName profile placeholder",
                    tint = Color(0xFF3B4260),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

private fun tiltForUser(id: Int): Float {
    val normalized = ((abs(id * 1103515245 + 12345) % 900).toFloat() / 100f) - 2.5f
    return normalized.coerceIn(-2.5f, 2.5f)
}

private fun formatReputation(reputation: Int): String {
    return when {
        reputation >= 1_000_000 -> {
            val value = reputation / 1_000_000f
            "${trimZeros(value)}M"
        }

        reputation >= 1_000 -> {
            val value = reputation / 1_000f
            "${trimZeros(value)}k"
        }

        else -> reputation.toString()
    }
}

@SuppressLint("DefaultLocale")
private fun trimZeros(value: Float): String {
    val oneDecimal = String.format("%.1f", value)
    return if (oneDecimal.endsWith(".0")) oneDecimal.dropLast(2) else oneDecimal
}