package com.tv.core.util

interface TvPlayerInteractionListener {
    fun onUserAction(action: TVUserAction, data: Any? = null) {}
}