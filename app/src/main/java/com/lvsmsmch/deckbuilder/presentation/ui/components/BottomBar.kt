package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.Builder
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.Library
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.More
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.Route
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.Saved
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

@Composable
fun BottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    NavigationBar(containerColor = DeckBuilderColors.SurfaceContainer) {
        TabItem(navController, destination, Library(), Icons.Outlined.GridView, R.string.nav_library)
        TabItem(navController, destination, Builder, Icons.Outlined.Build, R.string.nav_builder)
        TabItem(navController, destination, Saved, Icons.Outlined.Bookmark, R.string.nav_saved)
        TabItem(navController, destination, More, Icons.Outlined.MoreHoriz, R.string.nav_more)
    }
}

/** Reified-typed item — required because `navigate<T>(...)` and `hasRoute<T>()` need a concrete `T`. */
@Composable
private inline fun <reified T : Route> RowScope.TabItem(
    navController: NavController,
    destination: NavDestination?,
    route: T,
    icon: ImageVector,
    labelRes: Int,
) {
    val selected = destination?.hierarchy?.any { it.hasRoute(T::class) } == true
    NavigationBarItem(
        selected = selected,
        onClick = {
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
