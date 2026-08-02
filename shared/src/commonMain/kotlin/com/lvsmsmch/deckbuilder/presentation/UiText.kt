package com.lvsmsmch.deckbuilder.presentation

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Text produced outside composition (view models) but rendered inside it.
 * Keeps localization in resources instead of hardcoded English in view models,
 * while still allowing raw strings for messages coming from exceptions.
 */
sealed interface UiText {
    data class Raw(val value: String) : UiText
    data class Resource(val res: StringResource, val args: List<Any> = emptyList()) : UiText

    companion object {
        fun of(res: StringResource, vararg args: Any): UiText = Resource(res, args.toList())
    }
}

@Composable
fun UiText.resolve(): String = when (this) {
    is UiText.Raw -> value
    is UiText.Resource -> if (args.isEmpty()) stringResource(res) else stringResource(res, *args.toTypedArray())
}

suspend fun UiText.await(): String = when (this) {
    is UiText.Raw -> value
    is UiText.Resource -> if (args.isEmpty()) getString(res) else getString(res, *args.toTypedArray())
}
