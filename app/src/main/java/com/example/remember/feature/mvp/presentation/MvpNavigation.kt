package com.example.remember.feature.mvp.presentation

/**
 * Returns the destination for Android's system Back action.
 * A null destination delegates Back to the Activity, allowing the app to close from Today.
 */
internal fun MvpPage.backDestination(): MvpPage? = when (this) {
    MvpPage.TODAY -> null
    MvpPage.PROFILE,
    MvpPage.HISTORY,
    MvpPage.SETTINGS,
    MvpPage.PRIVACY -> MvpPage.MORE
    MvpPage.GOALS,
    MvpPage.AVATAR,
    MvpPage.STORE,
    MvpPage.MORE -> MvpPage.TODAY
}
