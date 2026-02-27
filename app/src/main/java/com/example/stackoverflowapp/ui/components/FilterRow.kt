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
import com.example.stackoverflowapp.ui.home.HomeViewModel

@Composable
fun FilterRow(viewModel: HomeViewModel) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        item {
            FilterChip(
                selected = viewModel.showFavoritesOnly,
                onClick = viewModel::toggleFavoritesFilter,
                label = { Text("Favorites") },
                leadingIcon = {
                    if (viewModel.showFavoritesOnly) Icon(
                        Icons.Default.Check,
                        null
                    )
                }
            )
        }
        item {
            FilterChip(
                selected = viewModel.sortOrder == HomeViewModel.SortOrder.NAME_ASC,
                onClick = {
                    viewModel.onSortOrderChange(
                        HomeViewModel.SortOrder.NAME_ASC
                    )
                },
                label = { Text("Name A-Z") }
            )
        }
        item {
            val isRepDesc =
                viewModel.sortOrder == HomeViewModel.SortOrder.REPUTATION_DESC
            FilterChip(
                selected = true,
                onClick = {
                    val next =
                        if (isRepDesc) HomeViewModel.SortOrder.REPUTATION_ASC else
                            HomeViewModel.SortOrder.REPUTATION_DESC
                    viewModel.onSortOrderChange(next)
                },
                label = { Text(if (isRepDesc) "Popularity ↓" else "Popularity ↑") }
            )
        }
    }
}