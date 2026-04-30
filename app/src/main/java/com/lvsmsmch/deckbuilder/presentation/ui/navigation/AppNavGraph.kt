package com.lvsmsmch.deckbuilder.presentation.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.data.update.UpdateEvent
import com.lvsmsmch.deckbuilder.data.update.UpdateNotifier
import com.lvsmsmch.deckbuilder.presentation.ui.components.BottomBar
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

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val onTopLevel = backStackEntry?.destination?.isTopLevel() ?: true
    val notifier: UpdateNotifier = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }
    val cardsUpdatedTemplate = stringResource(R.string.snackbar_cards_updated)

    LaunchedEffect(notifier) {
        notifier.events.collect { event ->
            when (event) {
                is UpdateEvent.CardsUpdated ->
                    snackbarHostState.showSnackbar(cardsUpdatedTemplate.format(event.build))
                is UpdateEvent.RotationUpdated -> Unit
            }
        }
    }

    Scaffold(
        bottomBar = {
            // Bottom bar shows only on top-level destinations. We render it
            // unconditionally and rely on the `onTopLevel` flag below to skip it
            // — animating visibility caused the bar to occasionally lag a frame
            // behind navigation transitions.
            if (onTopLevel) BottomBar(navController = navController)
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = DeckBuilderColors.SurfaceContainerHigh,
                    contentColor = DeckBuilderColors.OnSurface,
                ) { Text(data.visuals.message) }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Library(),
            modifier = Modifier.padding(padding),
            // Drop transitions: fadeIn/Out caused two cards to open back-to-back
            // when the user double-tapped quickly across cards. Instant swap.
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable<Library> { entry ->
                val args = entry.toRoute<Library>()
                CardLibraryScreen(
                    initialKeyword = args.initialKeyword,
                    initialSetSlug = args.initialSetSlug,
                    onCardClick = { card ->
                        navController.navigate(CardDetail(idOrSlug = card.id.toString()))
                    },
                )
            }
            composable<Builder> {
                DeckBuilderScreen(
                    onDeckSaved = { code ->
                        navController.navigate(DeckView(code = code)) {
                            popUpTo(Builder) { inclusive = true }
                        }
                    },
                )
            }
            composable<Saved> {
                SavedDecksScreen(
                    onOpenDeck = { code ->
                        navController.navigate(DeckView(code = code))
                    },
                    onCreateFromScratch = { navController.navigate(Builder) },
                )
            }
            composable<More> {
                MoreScreen(
                    onOpenSettings = { navController.navigate(Settings) },
                    onOpenCardData = { navController.navigate(CardData) },
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
                    onBack = { navController.navigateUp() },
                    onCardClick = { card ->
                        navController.navigate(CardDetail(idOrSlug = card.id.toString()))
                    },
                )
            }

            composable<Settings> {
                SettingsScreen(onBack = { navController.navigateUp() })
            }

            composable<CardData> {
                CardDataScreen(onBack = { navController.navigateUp() })
            }
        }
    }
}

private fun androidx.navigation.NavDestination.isTopLevel(): Boolean =
    hierarchy.any { d ->
        d.hasRoute(Library::class) ||
            d.hasRoute(Saved::class) ||
            d.hasRoute(More::class)
    }
