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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.BuildConfig
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.SupportedCardLocales
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.viewmodel.koinViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showLocalePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeckBuilderColors.Surface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onBack)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                item { SectionHeader(stringResource(R.string.settings_section_appearance)) }
                item {
                    GroupCard {
                        ThemeRow(
                            current = state.prefs.theme,
                            onChange = viewModel::setTheme,
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_section_language)) }
                item {
                    GroupCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLocalePicker = true }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_card_language),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeckBuilderColors.OnSurface,
                                )
                                Text(
                                    text = stringResource(R.string.settings_card_language_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeckBuilderColors.OnSurfaceDim,
                                )
                            }
                            Text(
                                text = SupportedCardLocales.displayName(state.prefs.cardLocale),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DeckBuilderColors.OnSurfaceDim,
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = "›",
                                style = MaterialTheme.typography.titleLarge,
                                color = DeckBuilderColors.OnSurfaceDimmer,
                            )
                        }
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_section_data)) }
                item {
                    GroupCard {
                        RefreshRow(
                            isRefreshing = state.isRefreshingMetadata,
                            refreshedAtMs = state.metadataRefreshedAtMs,
                            onClick = viewModel::refreshMetadataNow,
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_section_privacy)) }
                item {
                    GroupCard {
                        ToggleRow(
                            title = stringResource(R.string.settings_crash_reports),
                            subtitle = stringResource(R.string.settings_crash_reports_subtitle),
                            checked = state.prefs.crashReportingEnabled,
                            onCheckedChange = viewModel::setCrashReportingEnabled,
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_section_about)) }
                item {
                    GroupCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_version),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeckBuilderColors.OnSurface,
                                )
                                Text(
                                    text = BuildConfig.APPLICATION_ID,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeckBuilderColors.OnSurfaceDim,
                                )
                            }
                            Text(
                                text = BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DeckBuilderColors.OnSurfaceDim,
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
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

    if (showLocalePicker) {
        LocalePickerDialog(
            current = state.prefs.cardLocale,
            onPick = {
                viewModel.setLocale(it)
                showLocalePicker = false
            },
            onDismiss = { showLocalePicker = false },
        )
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
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = DeckBuilderColors.OnSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = DeckBuilderColors.OnSurfaceDim,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp, start = 4.dp),
    )
}

@Composable
private fun GroupCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DeckBuilderColors.SurfaceContainer)
            .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(14.dp)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun ThemeRow(current: ThemeMode, onChange: (ThemeMode) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        ThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChange(mode) }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = current == mode,
                    onClick = { onChange(mode) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = DeckBuilderColors.Primary,
                        unselectedColor = DeckBuilderColors.OnSurfaceDim,
                    ),
                )
                Spacer(Modifier.size(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            when (mode) {
                                ThemeMode.System -> R.string.settings_theme_system
                                ThemeMode.Dark -> R.string.settings_theme_dark
                                ThemeMode.Light -> R.string.settings_theme_light
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeckBuilderColors.OnSurface,
                    )
                    if (mode == ThemeMode.System) {
                        Text(
                            text = stringResource(R.string.settings_theme_system_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = DeckBuilderColors.OnSurfaceDim,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RefreshRow(
    isRefreshing: Boolean,
    refreshedAtMs: Long?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isRefreshing, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_refresh_metadata),
                style = MaterialTheme.typography.titleMedium,
                color = DeckBuilderColors.OnSurface,
            )
            Text(
                text = refreshedAtMs?.let { stringResource(R.string.settings_refresh_metadata_last, formatDate(it)) }
                    ?: stringResource(R.string.settings_refresh_metadata_subtitle),
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
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = DeckBuilderColors.OnSurfaceDimmer,
            )
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = DeckBuilderColors.OnSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = DeckBuilderColors.OnSurfaceDim,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = DeckBuilderColors.Primary,
                checkedThumbColor = DeckBuilderColors.OnPrimary,
            ),
        )
    }
}

@Composable
private fun LocalePickerDialog(
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeckBuilderColors.SurfaceContainer,
        title = { Text(stringResource(R.string.settings_card_language)) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
            ) {
                items(
                    count = SupportedCardLocales.codes.size,
                    key = { i -> SupportedCardLocales.codes[i].first },
                ) { i ->
                    val (code, name) = SupportedCardLocales.codes[i]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = code == current,
                            onClick = { onPick(code) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = DeckBuilderColors.Primary,
                                unselectedColor = DeckBuilderColors.OnSurfaceDim,
                            ),
                        )
                        Spacer(Modifier.size(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DeckBuilderColors.OnSurface,
                            )
                            Text(
                                text = code,
                                style = MaterialTheme.typography.bodySmall,
                                color = DeckBuilderColors.OnSurfaceDim,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
    )
}

private fun formatDate(ms: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(ms))
