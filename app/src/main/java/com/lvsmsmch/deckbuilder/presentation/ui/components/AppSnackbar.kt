package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Material3 only offers Short (4s) / Long (10s) snackbar durations, so an
 * exact app-wide duration has to be enforced manually: show as Indefinite and
 * cancel after [SNACKBAR_DURATION_MS].
 */
const val SNACKBAR_DURATION_MS = 2_500L

suspend fun SnackbarHostState.showAppSnackbar(
    message: String,
    actionLabel: String? = null,
): SnackbarResult {
    currentSnackbarData?.dismiss()
    return withTimeoutOrNull(SNACKBAR_DURATION_MS) {
        showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Indefinite,
        )
    } ?: SnackbarResult.Dismissed
}

@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) { data ->
        Snackbar(
            action = {
                data.visuals.actionLabel?.let { label ->
                    Text(
                        text = label,
                        color = DeckBuilderColors.Primary,
                        modifier = Modifier
                            .clickable { data.performAction() }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                    )
                }
            },
            containerColor = DeckBuilderColors.SurfaceContainerHigh,
            contentColor = DeckBuilderColors.OnSurface,
        ) {
            Text(
                text = data.visuals.message,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}
