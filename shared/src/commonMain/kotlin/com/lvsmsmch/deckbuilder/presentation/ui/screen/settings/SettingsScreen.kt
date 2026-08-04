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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import com.lvsmsmch.deckbuilder.presentation.platform.AppInfo
import com.lvsmsmch.deckbuilder.presentation.SnackbarController
import com.lvsmsmch.deckbuilder.presentation.UiText
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.*
import androidx.compose.ui.text.font.FontWeight
import com.lvsmsmch.deckbuilder.util.formatBytes
import com.lvsmsmch.deckbuilder.util.formatDateTime
import com.lvsmsmch.deckbuilder.data.debug.SessionLog
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.entities.SupportedCardLocales
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.presentation.ui.components.ScreenTopBar
import com.lvsmsmch.deckbuilder.presentation.ui.components.showAppSnackbar
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import com.lvsmsmch.deckbuilder.util.IoDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PRIVACY_POLICY_URL = "https://www.google.com"

@Composable
fun SettingsScreen(
    initialPreferences: AppPreferences,
    onBack: () -> Unit,
    onOpenCardData: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(parameters = { parametersOf(initialPreferences) }),
) {
    val state by viewModel.state.collectAsState()
    val platformContext = LocalPlatformContext.current
    val clipboard = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val snackbar: SnackbarController = koinInject()
    val appInfo: AppInfo = koinInject()
    val scope = rememberCoroutineScope()
    // Disk cache size is file-system IO — never compute it during composition
    // on the main thread.
    var imageCacheBytes by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        imageCacheBytes = withContext(IoDispatcher) { imageCacheSize(platformContext) }
    }
    val sessionLog: SessionLog = koinInject()
    var showThemePicker by remember { mutableStateOf(false) }
    var showLocalePicker by remember { mutableStateOf(false) }
    var showClearImageCacheConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.show(it)
            viewModel.dismissMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeckBuilderColors.Surface).statusBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onBack)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                item { SectionHeader(stringResource(Res.string.settings_section_appearance)) }
                item {
                    GroupCard {
                        DialogRow(
                            title = stringResource(Res.string.settings_theme_title),
                            subtitle = stringResource(Res.string.settings_theme_subtitle),
                            value = themeLabel(state.prefs.theme),
                            onClick = { showThemePicker = true },
                        )
                    }
                }

                item { SectionHeader(stringResource(Res.string.settings_section_language)) }
                item {
                    GroupCard {
                        DialogRow(
                            title = stringResource(Res.string.settings_card_language),
                            subtitle = stringResource(Res.string.settings_card_language_subtitle),
                            value = SupportedCardLocales.displayName(state.prefs.cardLocale),
                            onClick = { showLocalePicker = true },
                        )
                    }
                }

                item { SectionHeader(stringResource(Res.string.settings_section_privacy)) }
                item {
                    GroupCard {
                        ToggleRow(
                            title = stringResource(Res.string.settings_crash_reports),
                            subtitle = stringResource(Res.string.settings_crash_reports_subtitle),
                            checked = state.prefs.crashReportingEnabled,
                            onCheckedChange = viewModel::setCrashReportingEnabled,
                        )
                        Divider()
                        DialogRow(
                            title = stringResource(Res.string.settings_privacy_policy),
                            subtitle = stringResource(Res.string.settings_privacy_policy_subtitle),
                            value = "",
                            trailingIcon = Icons.Outlined.Language,
                            onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) },
                        )
                    }
                }

                item { SectionHeader(stringResource(Res.string.settings_section_storage)) }
                item {
                    GroupCard {
                        // Card data is a state, not a menu entry: say whether it
                        // is current, and when it was last checked.
                        StatusRow(
                            title = stringResource(Res.string.more_card_data),
                            subtitle = state.prefs.lastUpdateCheckAtMs
                                ?.let { stringResource(Res.string.settings_last_check_label, formatDateTime(it)) }
                                ?: stringResource(Res.string.settings_status_never_checked),
                            statusLabel = stringResource(
                                if (state.prefs.lastUpdateCheckAtMs != null) {
                                    Res.string.settings_status_up_to_date
                                } else {
                                    Res.string.action_retry
                                },
                            ),
                            statusOk = state.prefs.lastUpdateCheckAtMs != null,
                            onClick = onOpenCardData,
                        )
                        Divider()
                        DialogRow(
                            title = stringResource(Res.string.settings_cards_build),
                            subtitle = stringResource(
                                Res.string.settings_card_data_summary,
                                state.cardCount,
                                formatBytes(state.cardDataBytes),
                            ),
                            value = state.cardsBuild.orEmpty(),
                            trailingIcon = null,
                            onClick = onOpenCardData,
                        )
                        Divider()
                        DialogRow(
                            title = stringResource(Res.string.settings_image_cache),
                            subtitle = stringResource(Res.string.settings_image_cache_subtitle),
                            value = formatBytes(imageCacheBytes),
                            trailingIcon = null,
                            onClick = { showClearImageCacheConfirm = true },
                        )
                    }
                }

                item { SectionHeader(stringResource(Res.string.settings_section_about)) }
                item {
                    GroupCard {
                        DialogRow(
                            title = stringResource(Res.string.settings_contact_developer),
                            subtitle = stringResource(Res.string.settings_contact_developer_subtitle),
                            value = "",
                            trailingIcon = null,
                            onClick = { uriHandler.openUri("mailto:iamajavagod@gmail.com") },
                        )
                        Divider()
                        if (appInfo.isDebug) {
                            DialogRow(
                                title = stringResource(Res.string.settings_debug_copy_logs),
                                subtitle = stringResource(Res.string.settings_debug_copy_logs_subtitle),
                                value = "",
                                trailingIcon = null,
                                onClick = {
                                    clipboard.setText(AnnotatedString(sessionLog.dump()))
                                    snackbar.show(UiText.of(Res.string.settings_debug_logs_copied))
                                },
                            )
                            Divider()
                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = "${appInfo.applicationId} · ${appInfo.versionName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeckBuilderColors.OnSurfaceDimmer,
                        )
                        Text(
                            text = stringResource(Res.string.settings_footer_source),
                            style = MaterialTheme.typography.labelSmall,
                            color = DeckBuilderColors.OnSurfaceDimmer,
                        )
                    }
                }
            }
        }

    }

    if (showThemePicker) {
        ThemePickerDialog(
            current = state.prefs.theme,
            onPick = {
                viewModel.setTheme(it)
                showThemePicker = false
            },
            onDismiss = { showThemePicker = false },
        )
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

    if (showClearImageCacheConfirm) {
        AlertDialog(
            onDismissRequest = { showClearImageCacheConfirm = false },
            containerColor = DeckBuilderColors.SurfaceContainer,
            title = { Text(stringResource(Res.string.settings_clear_image_cache_title), color = DeckBuilderColors.OnSurface) },
            text = { Text(stringResource(Res.string.settings_clear_image_cache_message), color = DeckBuilderColors.OnSurface) },
            confirmButton = {
                TextButton(onClick = {
                    showClearImageCacheConfirm = false
                    scope.launch {
                        withContext(IoDispatcher) { clearImageCache(platformContext) }
                        imageCacheBytes = withContext(IoDispatcher) { imageCacheSize(platformContext) }
                        snackbar.show(UiText.of(Res.string.settings_image_cache_cleared))
                    }
                }) {
                    Text(stringResource(Res.string.action_clear), color = DeckBuilderColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearImageCacheConfirm = false }) {
                    Text(stringResource(Res.string.action_cancel), color = DeckBuilderColors.OnSurface)
                }
            },
        )
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    ScreenTopBar(title = stringResource(Res.string.settings_title), onBack = onBack)
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = DeckBuilderColors.OnSurfaceDimmer,
        modifier = Modifier.padding(top = 18.dp, bottom = 7.dp, start = 4.dp),
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
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DeckBuilderColors.OutlineSoft),
    )
}

@Composable
private fun DialogRow(
    title: String,
    subtitle: String,
    value: String,
    trailingIcon: ImageVector? = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = DeckBuilderColors.OnSurface,
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = DeckBuilderColors.OnSurfaceDimmer,
                )
            }
        }
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DeckBuilderColors.OnSurfaceDim,
            )
            Spacer(Modifier.size(6.dp))
        }
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = DeckBuilderColors.OnSurface,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** Settings row whose trailing slot reports state instead of navigating. */
@Composable
private fun StatusRow(
    title: String,
    subtitle: String,
    statusLabel: String,
    statusOk: Boolean,
    onClick: () -> Unit,
) {
    val color = if (statusOk) DeckBuilderColors.Success else DeckBuilderColors.Secondary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = DeckBuilderColors.OnSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = DeckBuilderColors.OnSurfaceDimmer,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = statusLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
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
            .clickable { onCheckedChange(!checked) }
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
                checkedTrackColor = DeckBuilderColors.OnSurface,
                checkedThumbColor = DeckBuilderColors.Surface,
                uncheckedTrackColor = DeckBuilderColors.SurfaceContainerHigh,
                uncheckedThumbColor = DeckBuilderColors.OnSurface,
                uncheckedBorderColor = DeckBuilderColors.OnSurface,
            ),
        )
    }
}

@Composable
private fun ThemePickerDialog(
    current: ThemeMode,
    onPick: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeckBuilderColors.SurfaceContainer,
        title = { Text(stringResource(Res.string.settings_theme_title), color = DeckBuilderColors.OnSurface) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(mode) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = current == mode,
                            onClick = { onPick(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = DeckBuilderColors.OnSurface,
                                unselectedColor = DeckBuilderColors.OnSurfaceDim,
                            ),
                        )
                        Spacer(Modifier.size(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = themeLabel(mode),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DeckBuilderColors.OnSurface,
                            )
                            if (mode == ThemeMode.System) {
                                Text(
                                    text = stringResource(Res.string.settings_theme_system_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeckBuilderColors.OnSurface,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_close), color = DeckBuilderColors.OnSurface) }
        },
    )
}

@Composable
private fun themeLabel(mode: ThemeMode): String = stringResource(
    when (mode) {
        ThemeMode.System -> Res.string.settings_theme_system
        ThemeMode.Dark -> Res.string.settings_theme_dark
        ThemeMode.Light -> Res.string.settings_theme_light
    },
)

@Composable
private fun LocalePickerDialog(
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeckBuilderColors.SurfaceContainer,
        title = { Text(stringResource(Res.string.settings_card_language), color = DeckBuilderColors.OnSurface) },
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
                                selectedColor = DeckBuilderColors.OnSurface,
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
                                color = DeckBuilderColors.OnSurface,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_close), color = DeckBuilderColors.OnSurface) }
        },
    )
}

private fun imageCacheSize(context: PlatformContext): Long {
    val loader = SingletonImageLoader.get(context)
    return (loader.memoryCache?.size ?: 0L) + (loader.diskCache?.size ?: 0L)
}

private fun clearImageCache(context: PlatformContext) {
    val loader = SingletonImageLoader.get(context)
    loader.memoryCache?.clear()
    loader.diskCache?.clear()
}


