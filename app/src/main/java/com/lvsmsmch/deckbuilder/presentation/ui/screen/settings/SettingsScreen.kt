package com.lvsmsmch.deckbuilder.presentation.ui.screen.settings

import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.imageLoader
import com.lvsmsmch.deckbuilder.BuildConfig
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.data.debug.SessionLog
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.entities.SupportedCardLocales
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.presentation.ui.components.AppSnackbarHost
import com.lvsmsmch.deckbuilder.presentation.ui.components.showAppSnackbar
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.Dispatchers
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
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Disk cache size is file-system IO — never compute it during composition
    // on the main thread.
    var imageCacheBytes by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        imageCacheBytes = withContext(Dispatchers.IO) { context.imageCacheSize() }
    }
    val sessionLog: SessionLog = koinInject()
    var showThemePicker by remember { mutableStateOf(false) }
    var showLocalePicker by remember { mutableStateOf(false) }
    var showClearImageCacheConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showAppSnackbar(it)
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
                item { SectionHeader(stringResource(R.string.settings_section_appearance)) }
                item {
                    GroupCard {
                        DialogRow(
                            title = stringResource(R.string.settings_theme_title),
                            subtitle = stringResource(R.string.settings_theme_subtitle),
                            value = themeLabel(state.prefs.theme),
                            onClick = { showThemePicker = true },
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_section_language)) }
                item {
                    GroupCard {
                        DialogRow(
                            title = stringResource(R.string.settings_card_language),
                            subtitle = stringResource(R.string.settings_card_language_subtitle),
                            value = SupportedCardLocales.displayName(state.prefs.cardLocale),
                            onClick = { showLocalePicker = true },
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
                        Divider()
                        DialogRow(
                            title = stringResource(R.string.settings_privacy_policy),
                            subtitle = stringResource(R.string.settings_privacy_policy_subtitle),
                            value = "",
                            trailingIcon = Icons.Outlined.Language,
                            onClick = { context.openInBrowser(PRIVACY_POLICY_URL) },
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_section_storage)) }
                item {
                    GroupCard {
                        DialogRow(
                            title = stringResource(R.string.more_card_data),
                            subtitle = stringResource(R.string.more_card_data_subtitle),
                            value = "",
                            onClick = onOpenCardData,
                        )
                        Divider()
                        DialogRow(
                            title = stringResource(R.string.settings_image_cache),
                            subtitle = stringResource(R.string.settings_image_cache_subtitle),
                            value = formatBytes(imageCacheBytes),
                            trailingIcon = null,
                            onClick = { showClearImageCacheConfirm = true },
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_section_about)) }
                item {
                    GroupCard {
                        DialogRow(
                            title = stringResource(R.string.settings_contact_developer),
                            subtitle = stringResource(R.string.settings_contact_developer_subtitle),
                            value = "",
                            trailingIcon = null,
                            onClick = { context.openEmail("iamajavagod@gmail.com") },
                        )
                        Divider()
                        if (BuildConfig.DEBUG) {
                            DialogRow(
                                title = stringResource(R.string.settings_debug_copy_logs),
                                subtitle = stringResource(R.string.settings_debug_copy_logs_subtitle),
                                value = "",
                                trailingIcon = null,
                                onClick = { context.copyLogs(sessionLog.dump()) },
                            )
                            Divider()
                        }
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

        AppSnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
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
            title = { Text(stringResource(R.string.settings_clear_image_cache_title), color = DeckBuilderColors.OnSurface) },
            text = { Text(stringResource(R.string.settings_clear_image_cache_message), color = DeckBuilderColors.OnSurface) },
            confirmButton = {
                TextButton(onClick = {
                    showClearImageCacheConfirm = false
                    scope.launch {
                        withContext(Dispatchers.IO) { context.clearImageCache() }
                        imageCacheBytes = withContext(Dispatchers.IO) { context.imageCacheSize() }
                        Toast.makeText(
                            context,
                            R.string.settings_image_cache_cleared,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }) {
                    Text(stringResource(R.string.action_clear), color = DeckBuilderColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearImageCacheConfirm = false }) {
                    Text(stringResource(R.string.action_cancel), color = DeckBuilderColors.OnSurface)
                }
            },
        )
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 8.dp),
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = DeckBuilderColors.OnSurface,
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = DeckBuilderColors.OnSurfaceDim,
                )
            }
        }
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = DeckBuilderColors.OnSurface,
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
        title = { Text(stringResource(R.string.settings_theme_title), color = DeckBuilderColors.OnSurface) },
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
                                    text = stringResource(R.string.settings_theme_system_subtitle),
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
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close), color = DeckBuilderColors.OnSurface) }
        },
    )
}

@Composable
private fun themeLabel(mode: ThemeMode): String = stringResource(
    when (mode) {
        ThemeMode.System -> R.string.settings_theme_system
        ThemeMode.Dark -> R.string.settings_theme_dark
        ThemeMode.Light -> R.string.settings_theme_light
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
        title = { Text(stringResource(R.string.settings_card_language), color = DeckBuilderColors.OnSurface) },
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
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close), color = DeckBuilderColors.OnSurface) }
        },
    )
}

private fun android.content.Context.openInBrowser(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { startActivity(intent) }
}

private fun android.content.Context.openEmail(email: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$email")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    }
    runCatching { startActivity(intent) }
}

private fun Context.copyLogs(logs: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Deck Builder debug log", logs))
    Toast.makeText(this, getString(R.string.settings_debug_logs_copied), Toast.LENGTH_SHORT).show()
}

private fun Context.imageCacheSize(): Long {
    val loader = imageLoader
    return (loader.memoryCache?.size ?: 0L) + (loader.diskCache?.size ?: 0L)
}

private fun Context.clearImageCache() {
    val loader = imageLoader
    loader.memoryCache?.clear()
    loader.diskCache?.clear()
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}
