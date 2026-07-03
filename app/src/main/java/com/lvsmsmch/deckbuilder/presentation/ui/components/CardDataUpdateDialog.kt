package com.lvsmsmch.deckbuilder.presentation.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonRepository
import com.lvsmsmch.deckbuilder.data.update.CardDataProgress
import com.lvsmsmch.deckbuilder.data.update.UpdateNotifier
import com.lvsmsmch.deckbuilder.data.update.UpdateRunner
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun CardDataUpdateDialog(
    required: Boolean,
    preferences: AppPreferences,
    onDismiss: () -> Unit,
    onExitApp: () -> Unit,
    forceRefresh: Boolean = true,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updateRunner: UpdateRunner = koinInject()
    val hsJson: HsJsonRepository = koinInject()
    val prefs: PreferencesRepository = koinInject()
    val notifier: UpdateNotifier = koinInject()
    val progress by notifier.cardDataProgress.collectAsState()
    var mode by remember { mutableStateOf(CardDataDialogMode.Checking) }
    var rememberMobileChoice by remember { mutableStateOf(false) }

    fun closeOrExit() {
        if (required) onExitApp() else onDismiss()
    }

    fun runUpdate() {
        mode = CardDataDialogMode.Running
        scope.launch {
            if (rememberMobileChoice) {
                prefs.setAllowMobileCardDataDownload(true)
            }
            runCatching { updateRunner.runOnce(reason = if (required) "startup gate" else "manual refresh") }
            val hasCards = runCatching { hsJson.cached(preferences.cardLocale) != null }.getOrDefault(false)
            mode = if (hasCards) {
                onDismiss()
                CardDataDialogMode.Done
            } else {
                CardDataDialogMode.Error
            }
        }
    }

    fun checkAndStart() {
        when (context.cardDataNetworkType()) {
            CardDataNetworkType.Wifi -> runUpdate()
            CardDataNetworkType.Mobile -> {
                if (preferences.allowMobileCardDataDownload) runUpdate()
                else mode = CardDataDialogMode.ConfirmMobile
            }
            CardDataNetworkType.None -> mode = CardDataDialogMode.NoNetwork
        }
    }

    LaunchedEffect(preferences.cardLocale) {
        val hasCards = runCatching { hsJson.cached(preferences.cardLocale) != null }.getOrDefault(false)
        if (!forceRefresh && hasCards) {
            onDismiss()
        } else {
            checkAndStart()
        }
    }

    when (mode) {
        CardDataDialogMode.Done -> Unit
        CardDataDialogMode.Checking -> {
            AlertDialog(
                onDismissRequest = {},
                containerColor = DeckBuilderColors.SurfaceContainer,
                title = { Text(stringResource(R.string.card_data_dialog_title)) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.library_loading_resolving),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeckBuilderColors.OnSurfaceDim,
                        )
                        Spacer(Modifier.height(14.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = DeckBuilderColors.Primary,
                            trackColor = DeckBuilderColors.SurfaceContainerHigh,
                        )
                    }
                },
                confirmButton = {},
            )
        }
        CardDataDialogMode.Running -> {
            AlertDialog(
                onDismissRequest = {},
                containerColor = DeckBuilderColors.SurfaceContainer,
                title = { Text(stringResource(R.string.card_data_dialog_title)) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = cardDataProgressText(progress),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeckBuilderColors.OnSurfaceDim,
                        )
                        Spacer(Modifier.height(14.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = DeckBuilderColors.Primary,
                            trackColor = DeckBuilderColors.SurfaceContainerHigh,
                        )
                    }
                },
                confirmButton = {},
            )
        }
        CardDataDialogMode.ConfirmMobile -> {
            AlertDialog(
                onDismissRequest = {},
                containerColor = DeckBuilderColors.SurfaceContainer,
                title = { Text(stringResource(R.string.card_data_mobile_title)) },
                text = {
                    Column {
                        Text(stringResource(R.string.card_data_mobile_message))
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMobileChoice,
                                onCheckedChange = { rememberMobileChoice = it },
                                colors = CheckboxDefaults.colors(checkedColor = DeckBuilderColors.Primary),
                            )
                            Text(
                                text = stringResource(R.string.card_data_mobile_remember),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { runUpdate() }) {
                        Text(stringResource(R.string.action_continue))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { closeOrExit() }) {
                        Text(
                            stringResource(if (required) R.string.action_exit else R.string.action_cancel),
                            color = DeckBuilderColors.OnSurface,
                        )
                    }
                },
            )
        }
        CardDataDialogMode.NoNetwork -> {
            AlertDialog(
                onDismissRequest = {},
                containerColor = DeckBuilderColors.SurfaceContainer,
                title = { Text(stringResource(R.string.card_data_no_network_title)) },
                text = { Text(stringResource(R.string.card_data_no_network_message)) },
                confirmButton = {
                    TextButton(onClick = { checkAndStart() }) {
                        Text(stringResource(R.string.action_retry))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { closeOrExit() }) {
                        Text(
                            stringResource(if (required) R.string.action_exit else R.string.action_close),
                            color = DeckBuilderColors.OnSurface,
                        )
                    }
                },
            )
        }
        CardDataDialogMode.Error -> {
            AlertDialog(
                onDismissRequest = {},
                containerColor = DeckBuilderColors.SurfaceContainer,
                title = { Text(stringResource(R.string.card_data_error_title)) },
                text = { Text(stringResource(R.string.card_data_error_message)) },
                confirmButton = {
                    TextButton(onClick = { checkAndStart() }) {
                        Text(stringResource(R.string.action_retry))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { closeOrExit() }) {
                        Text(
                            stringResource(if (required) R.string.action_exit else R.string.action_close),
                            color = DeckBuilderColors.OnSurface,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun cardDataProgressText(progress: CardDataProgress?): String {
    return when (progress?.stage) {
        CardDataProgress.Stage.RESOLVING_BUILD -> stringResource(R.string.library_loading_resolving)
        CardDataProgress.Stage.DOWNLOADING -> {
            val loaded = formatBytes(progress.downloadedBytes)
            val total = progress.totalBytes
            if (total != null) {
                val left = (total - progress.downloadedBytes).coerceAtLeast(0L)
                stringResource(R.string.library_loading_downloading_known, loaded, formatBytes(total), formatBytes(left))
            } else {
                stringResource(R.string.library_loading_downloading, loaded)
            }
        }
        CardDataProgress.Stage.PARSING -> stringResource(R.string.library_loading_parsing)
        CardDataProgress.Stage.SAVING -> stringResource(R.string.library_loading_saving)
        null -> stringResource(R.string.library_loading_cards)
    }
}

private fun Context.cardDataNetworkType(): CardDataNetworkType {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return CardDataNetworkType.None
    val network = manager.activeNetwork ?: return CardDataNetworkType.None
    val capabilities = manager.getNetworkCapabilities(network) ?: return CardDataNetworkType.None
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> CardDataNetworkType.Wifi
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> CardDataNetworkType.Mobile
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> CardDataNetworkType.Wifi
        else -> CardDataNetworkType.None
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

private enum class CardDataNetworkType { Wifi, Mobile, None }

private enum class CardDataDialogMode { Checking, ConfirmMobile, NoNetwork, Running, Error, Done }
