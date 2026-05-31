package com.lvsmsmch.deckbuilder.presentation.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CardDataScreen(
    initialPreferences: AppPreferences,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(parameters = { parametersOf(initialPreferences) }),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeckBuilderColors.Surface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = DeckBuilderColors.OnSurface,
                    )
                }
                Text(
                    text = stringResource(R.string.more_card_data),
                    style = MaterialTheme.typography.titleLarge,
                    color = DeckBuilderColors.OnSurface,
                    modifier = Modifier.weight(1f),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeckBuilderColors.SurfaceContainer)
                    .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(14.dp)),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    InfoRow(
                        title = stringResource(R.string.settings_cards_build),
                        value = state.cardsBuild
                            ?: stringResource(R.string.settings_cards_build_unknown),
                    )
                    InfoRow(
                        title = stringResource(R.string.settings_last_check),
                        value = formatLastCheck(state.prefs.lastUpdateCheckAtMs),
                    )
                }
            }

            RefreshCardDataRow(
                isRefreshing = state.isRefreshingCardData,
                onClick = viewModel::refreshCardDataNow,
            )
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { data ->
            Snackbar(
                containerColor = DeckBuilderColors.SurfaceContainerHigh,
                contentColor = DeckBuilderColors.OnSurface,
            ) { Text(data.visuals.message) }
        }
    }
}

@Composable
private fun RefreshCardDataRow(
    isRefreshing: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DeckBuilderColors.SurfaceContainer)
            .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(14.dp))
            .clickable(enabled = !isRefreshing, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_refresh_card_data),
                style = MaterialTheme.typography.titleMedium,
                color = DeckBuilderColors.OnSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.settings_refresh_card_data_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = DeckBuilderColors.OnSurfaceDim,
            )
        }
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = DeckBuilderColors.Primary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = ">",
                style = MaterialTheme.typography.titleLarge,
                color = DeckBuilderColors.OnSurfaceDimmer,
            )
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = DeckBuilderColors.OnSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurfaceDim,
        )
    }
}

@Composable
private fun formatLastCheck(epochMs: Long?): String {
    if (epochMs == null) return stringResource(R.string.settings_last_check_never)
    val fmt = java.text.DateFormat.getDateTimeInstance(
        java.text.DateFormat.MEDIUM,
        java.text.DateFormat.SHORT,
    )
    return fmt.format(java.util.Date(epochMs))
}
