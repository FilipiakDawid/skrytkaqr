package io.github.filipiakdawid.skrytkaqr.ui

sealed class Screen(val route: String) {
    object Active : Screen("active")

    object Trash : Screen("trash")

    object Settings : Screen("settings")

    object QrDetail : Screen("qr/{parcelId}")

    object About : Screen("about")
}

fun qrDetailRoute(id: Long) = "qr/$id"
