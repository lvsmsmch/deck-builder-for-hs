package com.lvsmsmch.deckbuilder.util

import kotlinx.coroutines.CoroutineDispatcher

/** Dispatchers.IO is not exposed to common metadata; both targets provide it. */
expect val IoDispatcher: CoroutineDispatcher
