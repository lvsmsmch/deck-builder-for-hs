package com.lvsmsmch.deckbuilder.presentation.ui.screen.cardbacks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.CardBack
import com.lvsmsmch.deckbuilder.domain.entities.CardBackCategory
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CardBacksScreen(
    onBack: () -> Unit,
    viewModel: CardBacksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val gridState = rememberLazyGridState()

    val nearEnd by remember {
        derivedStateOf {
            val total = gridState.layoutInfo.totalItemsCount
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 6
        }
    }
    LaunchedEffect(gridState) {
        snapshotFlow { nearEnd }.distinctUntilChanged().collect { atEnd ->
            if (atEnd) viewModel.loadNextPage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckBuilderColors.Surface),
    ) {
        TopBar(onBack)
        SearchField(state.textQuery, viewModel::setQuery)
        CategoryChips(active = state.category, onSelect = viewModel::setCategory)

        when {
            state.isLoadingFirstPage && state.items.isEmpty() -> CenteredSpinner()
            state.errorMessage != null && state.items.isEmpty() -> ErrorState(
                message = state.errorMessage!!,
                onRetry = viewModel::retry,
            )
            state.items.isEmpty() -> EmptyState()
            else -> Grid(state, gridState, onPick = viewModel::select)
        }
    }

    state.selected?.let { item ->
        CardBackDetail(item = item, onDismiss = { viewModel.select(null) })
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = DeckBuilderColors.OnSurface,
            )
        }
        Text(
            text = stringResource(R.string.cardbacks_title),
            style = MaterialTheme.typography.titleLarge,
            color = DeckBuilderColors.OnSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onChange,
        placeholder = { Text(stringResource(R.string.cardbacks_search_hint), color = DeckBuilderColors.OnSurfaceDimmer) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = DeckBuilderColors.OnSurfaceDim) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Outlined.Close,
                    null,
                    tint = DeckBuilderColors.OnSurfaceDim,
                    modifier = Modifier.clickable { onChange("") }.padding(4.dp),
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DeckBuilderColors.SurfaceContainer,
            unfocusedContainerColor = DeckBuilderColors.SurfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = DeckBuilderColors.OnSurface,
            unfocusedTextColor = DeckBuilderColors.OnSurface,
            cursorColor = DeckBuilderColors.Primary,
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )
}

@Composable
private fun CategoryChips(active: String?, onSelect: (String?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Chip(label = stringResource(R.string.cardbacks_category_all), active = active == null, onClick = { onSelect(null) })
        CardBackCategory.Known.forEach { (slug, label) ->
            Chip(label = label, active = active == slug, onClick = { onSelect(slug) })
        }
    }
}

@Composable
private fun Chip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(
                if (active) DeckBuilderColors.PrimarySoft else DeckBuilderColors.SurfaceContainer,
            )
            .border(
                1.dp,
                if (active) DeckBuilderColors.Primary else DeckBuilderColors.OutlineSoft,
                RoundedCornerShape(99.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) DeckBuilderColors.Primary else DeckBuilderColors.OnSurfaceDim,
        )
    }
}

@Composable
private fun Grid(
    state: CardBacksState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onPick: (CardBack) -> Unit,
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.items, key = { it.id }) { back ->
            Box(
                modifier = Modifier
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF3A2E57),
                                Color(0xFF1A1429),
                            ),
                        ),
                    )
                    .border(1.dp, DeckBuilderColors.Outline, RoundedCornerShape(10.dp))
                    .clickable { onPick(back) },
            ) {
                if (back.image.isNotBlank()) {
                    AsyncImage(
                        model = back.image,
                        contentDescription = back.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
        if (state.isLoadingMore || state.hasMore) {
            item(span = { GridItemSpan(3) }) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = DeckBuilderColors.Primary,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CardBackDetail(item: CardBack, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeckBuilderColors.SurfaceContainer,
        title = { Text(item.name) },
        text = {
            Column {
                if (item.image.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(0.7f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeckBuilderColors.SurfaceContainerHigh),
                    ) {
                        AsyncImage(
                            model = item.image,
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                if (!item.text.isNullOrBlank()) {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeckBuilderColors.OnSurfaceDim,
                    )
                }
                if (!item.sortCategory.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Category: ${item.sortCategory}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeckBuilderColors.OnSurfaceDimmer,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
    )
}

@Composable
private fun CenteredSpinner() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = DeckBuilderColors.Primary, strokeWidth = 2.dp)
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.cardbacks_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurfaceDim,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = DeckBuilderColors.Error, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DeckBuilderColors.Primary)
                .clickable(onClick = onRetry)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Text(stringResource(R.string.action_retry), color = DeckBuilderColors.OnPrimary, style = MaterialTheme.typography.labelLarge)
        }
    }
}
