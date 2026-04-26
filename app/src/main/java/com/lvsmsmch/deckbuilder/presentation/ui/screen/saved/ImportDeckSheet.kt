package com.lvsmsmch.deckbuilder.presentation.ui.screen.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDeckSheet(
    isImporting: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onErrorDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var code by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeckBuilderColors.SurfaceContainer,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DeckBuilderColors.Outline),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.import_title),
                style = MaterialTheme.typography.titleMedium,
                color = DeckBuilderColors.OnSurface,
            )
            Text(
                text = stringResource(R.string.import_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = DeckBuilderColors.OnSurfaceDim,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeckBuilderColors.SurfaceContainerHigh)
                    .border(1.dp, DeckBuilderColors.Outline, RoundedCornerShape(12.dp))
                    .padding(12.dp),
            ) {
                BasicTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        if (error != null) onErrorDismiss()
                    },
                    textStyle = TextStyle(
                        color = DeckBuilderColors.OnSurface,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(DeckBuilderColors.Primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    decorationBox = { inner ->
                        if (code.isEmpty()) {
                            Text(
                                text = "AAECAa0GBImsBPusBPCsBP+sBJyz...",
                                style = TextStyle(
                                    color = DeckBuilderColors.OnSurfaceDimmer,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                ),
                            )
                        }
                        inner()
                    },
                )
            }

            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = DeckBuilderColors.Error,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isImporting,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.action_cancel)) }

                Button(
                    onClick = { onSubmit(code) },
                    enabled = !isImporting && code.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeckBuilderColors.Primary,
                        contentColor = DeckBuilderColors.OnPrimary,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = DeckBuilderColors.OnPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.action_decode))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
