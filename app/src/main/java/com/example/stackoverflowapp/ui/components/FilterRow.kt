package com.example.stackoverflowapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stackoverflowapp.ui.home.SortField
import com.example.stackoverflowapp.ui.home.SortOrder

/**
 * A row of filters and sorting options for the Home screen.
 *
 * Provides a toggle for favorites and a dropdown/toggle combination for sorting.
 *
 * @param sortOrder The current active sorting configuration.
 * @param showFavouritesOnly Whether the favorites filter is currently active.
 * @param onToggleFavorites Callback invoked when the favorites filter is toggled.
 * @param onSortOrderChange Callback invoked when a new sort field or direction is selected.
 */
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
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        item {
            FilterChip(
                selected = showFavouritesOnly,
                onClick = onToggleFavorites,
                label = { Text("Favorites") },
                leadingIcon = {
                    if (showFavouritesOnly) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }

        item {
            var expanded by remember { mutableStateOf(false) }

            Box {
                AssistChip(
                    onClick = { expanded = true },
                    label = { Text(sortOrder.field.displayName) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    SortField.entries.forEach { field ->
                        val isSelected = sortOrder.field == field
                        DropdownMenuItem(
                            text = { Text(field.displayName) },
                            onClick = {
                                onSortOrderChange(sortOrder.copy(field = field))
                                expanded = false
                            },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        item {
            IconButton(
                onClick = { onSortOrderChange(sortOrder.toggleDirection()) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = sortOrder.direction.icon,
                    contentDescription = sortOrder.direction.contentDescription,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
