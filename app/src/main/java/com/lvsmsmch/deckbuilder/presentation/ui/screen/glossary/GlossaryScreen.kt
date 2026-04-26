package com.lvsmsmch.deckbuilder.presentation.ui.screen.glossary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GlossaryScreen(
    onBack: () -> Unit,
    onKeywordClick: (slug: String) -> Unit,
    viewModel: GlossaryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckBuilderColors.Surface),
    ) {
        TopBar(onBack)
        SearchField(state.query, viewModel::setQuery)

        when {
            !state.isMetadataReady -> CenteredSpinnerWithHint(stringResource(R.string.glossary_loading))
            state.groups.isEmpty() -> EmptyState(state.query)
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                state.groups.forEach { group ->
                    item(key = "letter-${group.letter}") {
                        Text(
                            text = group.letter,
                            style = MaterialTheme.typography.labelSmall,
                            color = DeckBuilderColors.OnSurfaceDimmer,
                            modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
                        )
                    }
                    items(
                        count = group.items.size,
                        key = { i -> "kw-${group.items[i].id}" },
                    ) { i ->
                        val kw = group.items[i]
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onKeywordClick(kw.slug) }
                                .padding(vertical = 12.dp),
                        ) {
                            Text(
                                text = kw.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = DeckBuilderColors.OnSurface,
                            )
                            if (kw.refText.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = kw.refText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeckBuilderColors.OnSurfaceDim,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(DeckBuilderColors.OutlineSoft),
                        )
                    }
                }
            }
        }
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
            text = stringResource(R.string.glossary_title),
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
        placeholder = { Text(stringResource(R.string.glossary_search_hint), color = DeckBuilderColors.OnSurfaceDimmer) },
        singleLine = true,
        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = DeckBuilderColors.OnSurfaceDim) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Clear",
                    tint = DeckBuilderColors.OnSurfaceDim,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onChange("") }
                        .padding(4.dp),
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DeckBuilderColors.SurfaceContainer,
            unfocusedContainerColor = DeckBuilderColors.SurfaceContainer,
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
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
private fun CenteredSpinnerWithHint(hint: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = DeckBuilderColors.Primary, strokeWidth = 2.dp)
        Spacer(Modifier.height(12.dp))
        Text(hint, color = DeckBuilderColors.OnSurfaceDim, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyState(query: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = if (query.isBlank()) {
                stringResource(R.string.glossary_empty_no_query)
            } else {
                stringResource(R.string.glossary_empty_query, query)
            },
            color = DeckBuilderColors.OnSurfaceDim,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
