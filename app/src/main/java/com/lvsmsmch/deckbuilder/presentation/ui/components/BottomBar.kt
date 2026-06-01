package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.Library
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.More
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.Route
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.Saved
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

@Composable
fun BottomBar(navController: NavController, destination: NavDestination?) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Library) }
    LaunchedEffect(destination) {
        destination?.toBottomTab()?.let { selectedTab = it }
    }

    NavigationBar(containerColor = DeckBuilderColors.SurfaceContainer) {
        TabItem(navController, selectedTab, BottomTab.Library, Library(), Icons.Outlined.GridView, R.string.nav_library) {
            selectedTab = BottomTab.Library
        }
        TabItem(navController, selectedTab, BottomTab.Saved, Saved, Icons.Outlined.Bookmark, R.string.nav_saved) {
            selectedTab = BottomTab.Saved
        }
        TabItem(navController, selectedTab, BottomTab.More, More, Icons.Outlined.MoreHoriz, R.string.nav_more) {
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
    icon: ImageVector,
    labelRes: Int,
    crossinline onSelected: () -> Unit,
) {
    NavigationBarItem(
        selected = selectedTab == tab,
        onClick = {
            onSelected()
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(stringResource(labelRes)) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = DeckBuilderColors.Primary,
            selectedTextColor = DeckBuilderColors.Primary,
            indicatorColor = DeckBuilderColors.PrimarySoft,
            unselectedIconColor = DeckBuilderColors.OnSurfaceDimmer,
            unselectedTextColor = DeckBuilderColors.OnSurfaceDimmer,
        ),
    )
}

private enum class BottomTab { Library, Saved, More }

private fun NavDestination.toBottomTab(): BottomTab? = when {
    hierarchy.any { it.hasRoute(Library::class) } -> BottomTab.Library
    hierarchy.any { it.hasRoute(Saved::class) } -> BottomTab.Saved
    hierarchy.any { it.hasRoute(More::class) } -> BottomTab.More
    else -> null
}
