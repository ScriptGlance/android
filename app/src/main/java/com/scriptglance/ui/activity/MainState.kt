package com.scriptglance.ui.activity

sealed class MainState {
    object Loading : MainState()
    object Authenticated : MainState()
    object Unauthenticated : MainState()
}