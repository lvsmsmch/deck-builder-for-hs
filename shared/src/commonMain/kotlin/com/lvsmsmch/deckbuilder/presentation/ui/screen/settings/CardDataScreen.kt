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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lvsmsmch.deckbuilder.util.formatBytes
import com.lvsmsmch.deckbuilder.util.formatDateTime
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.entities.SupportedCardLocales
import com.lvsmsmch.deckbuilder.presentation.SnackbarController
import com.lvsmsmch.deckbuilder.presentation.UiText
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardDataUpdateDialog
import com.lvsmsmch.deckbuilder.presentation.ui.components.ScreenTopBar
import com.lvsmsmch.deckbuilder.presentation.ui.components.showAppSnackbar
import com.lvsmsmch.deckbuilder.presentation.ui.components.Backdrop
import com.lvsmsmch.deckbuilder.presentation.ui.components.classAtmosphere
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CardDataScreen(
    initialPreferences: AppPreferences,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(parameters = { parametersOf(initialPreferences) }),
) {
    val state by viewModel.state.collectAsState()
    val snackbar: SnackbarController = koinInject()
    var showRefreshDialog by remember { mutableStateOf(false) }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.show(it)
            viewModel.dismissMessage()
        }
    }
    LaunchedEffect(state.prefs.cardLocale) {
        viewModel.refreshCardDataMetadata()
    }

    Backdrop(atmosphere = classAtmosphere("priest")) {
      Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenTopBar(
                title = stringResource(Res.string.more_card_data),
                onBack = onBack,
            )

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
                        title = stringResource(Res.string.settings_cards_build),
                        value = state.cardsBuild.orEmpty(),
                    )
                    RowDivider()
                    InfoRow(
                        title = stringResource(Res.string.settings_last_check),
                        value = formatLastCheck(state.prefs.lastUpdateCheckAtMs),
                    )
                    RowDivider()
                    InfoRow(
                        title = stringResource(Res.string.settings_card_data_locale),
                        value = SupportedCardLocales.displayName(state.prefs.cardLocale),
                    )
                    RowDivider()
                    InfoRow(
                        title = stringResource(Res.string.settings_card_data_cards),
                        value = state.cardCount.toString(),
                    )
                    RowDivider()
                    InfoRow(
                        title = stringResource(Res.string.settings_card_data_size),
                        value = formatBytes(state.cardDataBytes),
                    )
                }
            }

            RefreshCardDataRow(
                isRefreshing = state.isRefreshingCardData,
                onClick = { showRefreshDialog = true },
            )
        }

    }

    if (showRefreshDialog) {
        CardDataUpdateDialog(
            required = false,
            preferences = state.prefs,
            onDismiss = {
                showRefreshDialog = false
                viewModel.refreshCardDataMetadata()
            },
            onExitApp = onBack,
            onResult = { updated ->
                if (!updated) viewModel.showMessage(UiText.of(Res.string.card_data_up_to_date))
            },
        )
    }
  }
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DeckBuilderColors.OutlineSoft),
    )
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
                text = stringResource(Res.string.settings_refresh_card_data),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = DeckBuilderColors.OnSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(Res.string.settings_refresh_card_data_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = DeckBuilderColors.OnSurfaceDimmer,
            )
        }
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = DeckBuilderColors.Primary,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                tint = DeckBuilderColors.OnSurface,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = DeckBuilderColors.OnSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = DeckBuilderColors.OnSurfaceDim,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun formatLastCheck(epochMs: Long?): String {
    if (epochMs == null) return stringResource(Res.string.settings_last_check_never)
    return formatDateTime(epochMs)
}


