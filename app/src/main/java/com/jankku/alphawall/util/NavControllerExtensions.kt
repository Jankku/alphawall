package com.jankku.alphawall.util

import androidx.navigation.NavController
import androidx.navigation.NavDirections

fun NavController.navigateSafe(directions: NavDirections) {
    val navigateWillError = currentDestination?.getAction(directions.actionId) == null

    if (navigateWillError) {
        if (previousBackStackEntry?.destination?.getAction(directions.actionId) != null) return
    }

    navigate(directions)
}

fun NavController.navigateSafe(directions: Int) {
    val navigateWillError = currentDestination?.getAction(directions) == null

    if (navigateWillError) {
        if (previousBackStackEntry?.destination?.getAction(directions) != null) return
    }

    navigate(directions)
}
