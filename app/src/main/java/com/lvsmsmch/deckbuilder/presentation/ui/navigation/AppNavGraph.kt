package com.lvsmsmch.deckbuilder.presentation.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lvsmsmch.deckbuilder.presentation.ui.components.BottomBar
import com.lvsmsmch.deckbuilder.presentation.ui.screen.builder.DeckBuilderScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.deckview.DeckViewScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.detail.CardDetailScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.library.CardLibraryScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.more.MoreScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.saved.SavedDecksScreen
import com.lvsmsmch.deckbuilder.presentation.ui.screen.settings.SettingsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val onTopLevel = backStackEntry?.destination?.isTopLevel() ?: true

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = onTopLevel, enter = fadeIn(), exit = fadeOut()) {
                BottomBar(navController = navController)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Library(),
            modifier = Modifier.padding(padding),
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
                            popUpTo(Builder) { inclusive = false }
                        }
                    },
                )
            }
            composable<Saved> {
                SavedDecksScreen(
                    onOpenDeck = { code ->
                        navController.navigate(DeckView(code = code))
                    },
                )
            }
            composable<More> {
                MoreScreen(
                    onOpenSettings = { navController.navigate(Settings) },
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
        }
    }
}

private fun androidx.navigation.NavDestination.isTopLevel(): Boolean =
    hierarchy.any { d ->
        d.hasRoute(Library::class) ||
            d.hasRoute(Builder::class) ||
            d.hasRoute(Saved::class) ||
            d.hasRoute(More::class)
    }
