package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.Cards
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.More
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.Route
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.Saved
import com.lvsmsmch.deckbuilder.presentation.ui.theme.AppType
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.nav_decks
import com.lvsmsmch.deckbuilder.resources.nav_library
import com.lvsmsmch.deckbuilder.resources.nav_more
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Three words on a floating pane of glass, clear of the screen edge. The
 * selected tab is a brighter pane inside it rather than an underline — this
 * design says yes with light, not with rules.
 */
@Composable
fun BottomBar(navController: NavController, destination: NavDestination?) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Decks) }
    LaunchedEffect(destination) {
        destination?.toBottomTab()?.let { selectedTab = it }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(CircleShape)
            .background(DeckBuilderColors.SurfaceContainer)
            .border(1.dp, DeckBuilderColors.Outline, CircleShape)
            .padding(5.dp),
    ) {
        TabItem(navController, selectedTab, BottomTab.Decks, Saved, Res.string.nav_decks) {
            selectedTab = BottomTab.Decks
        }
        TabItem(navController, selectedTab, BottomTab.Cards, Cards, Res.string.nav_library) {
            selectedTab = BottomTab.Cards
        }
        TabItem(navController, selectedTab, BottomTab.More, More, Res.string.nav_more) {
            selectedTab = BottomTab.More
        }
    }
}

@Composable
private inline fun <reified T : Route> RowScope.TabItem(
    navController: NavController,
    selectedTab: BottomTab,
    tab: BottomTab,
    route: T,
    labelRes: StringResource,
    crossinline onSelected: () -> Unit,
) {
    val selected = selectedTab == tab
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(CircleShape)
            .background(
                if (selected) DeckBuilderColors.SurfaceContainerHighest else Color.Transparent,
            )
            .clickable {
                onSelected()
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(labelRes).uppercase(),
            style = AppType.micro,
            color = if (selected) DeckBuilderColors.OnSurface else DeckBuilderColors.OnSurfaceDimmer,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

private enum class BottomTab { Decks, Cards, More }

private fun NavDestination.toBottomTab(): BottomTab? = when {
    hierarchy.any { it.hasRoute(Saved::class) } -> BottomTab.Decks
    hierarchy.any { it.hasRoute(Cards::class) } -> BottomTab.Cards
    hierarchy.any { it.hasRoute(More::class) } -> BottomTab.More
    else -> null
}
