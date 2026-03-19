package com.example.stackoverflowapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stackoverflowapp.ui.home.SortOrder

@Composable
fun FilterRow(
    sortOrder: SortOrder,
    showFavouritesOnly: Boolean,
    onToggleFavorites: () -> Unit,
    onSortOrderChange: (SortOrder) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        item {
            FilterChip(
                selected = showFavouritesOnly,
                onClick = onToggleFavorites,
                label = { Text("Favorites") },
                leadingIcon = {
                    if (showFavouritesOnly) Icon(
                        Icons.Default.Check,
                        null
                    )
                }
            )
        }
        item {
            FilterChip(
                selected = sortOrder == SortOrder.NAME_ASC,
                onClick = {
                    onSortOrderChange(
                        SortOrder.NAME_ASC
                    )
                },
                label = { Text("Name A-Z") }
            )
        }
        item {
            val isRepDesc =
                sortOrder == SortOrder.REPUTATION_DESC
            FilterChip(
                selected = true,
                onClick = {
                    val next =
                        if (isRepDesc) SortOrder.REPUTATION_ASC else
                            SortOrder.REPUTATION_DESC
                    onSortOrderChange(next)
                },
                label = { Text(if (isRepDesc) "Popularity ↓" else "Popularity ↑") }
            )
        }
    }
}
