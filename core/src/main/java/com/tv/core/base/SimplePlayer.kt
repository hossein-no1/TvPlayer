package com.tv.core.base

import android.content.Context
import com.tv.core.ui.TvPlayerView

internal class SimplePlayer(
    context: Context,
    private val tvPlayerView: TvPlayerView,
    isLive: Boolean = false,
    playWhenReady: Boolean = true
) : TvPlayer(context, tvPlayerView, isLive, playWhenReady) {

    override fun release() {
        tvPlayerView.playerView.player = null
        player.release()
    }

}