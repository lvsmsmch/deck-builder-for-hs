package com.lvsmsmch.deckbuilder.presentation.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.data.update.UpdateEvent
import com.lvsmsmch.deckbuilder.data.update.UpdateNotifier
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.presentation.ui.components.BottomBar
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardDataUpdateDialog
import com.lvsmsmch.deckbuilder.presentation.ui.screen.builder.DeckBuilderScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.deckview.DeckViewScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.detail.CardDetailScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.library.CardLibraryScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.more.MoreScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.saved.SavedDecksScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.settings.CardDataScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.settings.SettingsScreen
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.koinInject

private const val SCREEN_FADE_IN_MS = 260
private const val SCREEN_FADE_OUT_MS = 220

@Composable
fun AppNavGraph(currentPreferences: AppPreferences) {
    val navController = rememberNavController()
    val notifier: UpdateNotifier = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }
    val cardsUpdatedTemplate = stringResource(R.string.snackbar_cards_updated)
    val context = LocalContext.current
    var showStartupCardDataDialog by remember { mutableStateOf(true) }

    LaunchedEffect(notifier) {
        notifier.events.collect { event ->
            when (event) {
                is UpdateEvent.CardsUpdated ->
                    snackbarHostState.showSnackbar(cardsUpdatedTemplate.format(event.build))
                is UpdateEvent.RotationUpdated -> Unit
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Home,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(tween(SCREEN_FADE_IN_MS)) },
            exitTransition = { fadeOut(tween(SCREEN_FADE_OUT_MS)) },
            popEnterTransition = { fadeIn(tween(SCREEN_FADE_IN_MS)) },
            popExitTransition = { fadeOut(tween(SCREEN_FADE_OUT_MS)) },
        ) {
            composable<Home> {
                HomeScreen(
                    onOpenDeck = { code, savedName -> navController.navigate(DeckView(code = code, savedName = savedName)) },
                    onCreateDeck = { navController.navigate(Builder()) },
                    onOpenSettings = { navController.navigate(Settings) },
                    onOpenCardLibrary = { navController.navigate(Library()) },
                )
            }
            composable<Library> { entry ->
                val args = entry.toRoute<Library>()
                CardLibraryScreen(
                    initialKeyword = args.initialKeyword,
                    initialSetSlug = args.initialSetSlug,
                    onCardClick = { card -> navController.navigate(CardDetail(idOrSlug = card.id.toString())) },
                )
            }
            composable<Builder> { entry ->
                val args = entry.toRoute<Builder>()
                DeckBuilderScreen(
                    editCode = args.editCode,
                    savedName = args.savedName,
                    onDeckSaved = { code ->
                        navController.navigate(DeckView(code = code, savedName = args.savedName)) {
                            if (args.editCode != null) {
                                popUpTo(DeckView(code = args.editCode, savedName = args.savedName)) {
                                    inclusive = true
                                }
                            } else {
                                popUpTo(Builder(args.editCode, args.savedName)) { inclusive = true }
                            }
                        }
                    },
                    onExit = { navController.navigateUp() },
                    onOpenCard = { card -> navController.navigate(CardDetail(idOrSlug = card.id.toString())) },
                )
            }
            composable<CardDetail> { entry ->
                val args = entry.toRoute<CardDetail>()
                CardDetailScreen(
                    idOrSlug = args.idOrSlug,
                    onBack = { navController.navigateUp() },
                    onCardClick = { card ->
                        navController.navigate(CardDetail(idOrSlug = card.id.toString()))
                    },
                )
            }

            composable<DeckView> { entry ->
                val args = entry.toRoute<DeckView>()
                DeckViewScreen(
                    code = args.code,
                    initialSavedName = args.savedName,
                    onBack = { navController.navigateUp() },
                    onEditDeck = { navController.navigate(Builder(editCode = args.code, savedName = args.savedName)) },
                    onCardClick = { card ->
                        navController.navigate(CardDetail(idOrSlug = card.id.toString()))
                    },
                )
            }

            composable<Settings> {
                SettingsScreen(
                    initialPreferences = currentPreferences,
                    onBack = { navController.navigateUp() },
                    onOpenCardData = { navController.navigate(CardData) },
                )
            }

            composable<CardData> {
                CardDataScreen(
                    initialPreferences = currentPreferences,
                    onBack = { navController.navigateUp() },
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { data ->
            Snackbar(
                containerColor = DeckBuilderColors.SurfaceContainerHigh,
                contentColor = DeckBuilderColors.OnSurface,
            ) { Text(data.visuals.message) }
        }

        if (showStartupCardDataDialog) {
            CardDataUpdateDialog(
                required = true,
                preferences = currentPreferences,
                onDismiss = { showStartupCardDataDialog = false },
                onExitApp = { (context as? Activity)?.finish() },
                forceRefresh = false,
            )
        }
    }
}

@Composable
private fun HomeScreen(
    onOpenDeck: (String, String?) -> Unit,
    onCreateDeck: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCardLibrary: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    var lastBackAt by remember { mutableLongStateOf(0L) }

    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackAt <= 2_000L) {
            (context as? Activity)?.finish()
        } else {
            lastBackAt = now
            Toast.makeText(context, R.string.action_press_back_again_exit, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            BottomBar(navController = navController, destination = backStackEntry?.destination)
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Saved,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(SCREEN_FADE_IN_MS)) },
            exitTransition = { fadeOut(tween(SCREEN_FADE_OUT_MS)) },
            popEnterTransition = { fadeIn(tween(SCREEN_FADE_IN_MS)) },
            popExitTransition = { fadeOut(tween(SCREEN_FADE_OUT_MS)) },
        ) {
            composable<Saved> {
                SavedDecksScreen(
                    onOpenDeck = onOpenDeck,
                    onCreateFromScratch = onCreateDeck,
                )
            }
            composable<More> {
                MoreScreen(
                    onOpenSettings = onOpenSettings,
                    onOpenCardLibrary = onOpenCardLibrary,
                )
            }
        }
    }
}
