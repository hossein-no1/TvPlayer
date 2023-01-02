package com.tv.core.base

import android.content.Context
import com.tv.core.ui.TvAdvertisePlayerView
import com.tv.core.ui.TvPlayerView

internal class SimplePlayer(
    context: Context,
    private val playerView: TvPlayerView,
    isLive: Boolean = false,
    playWhenReady: Boolean = true
) : BasePlayer(context, playerView, isLive, playWhenReady) {

    override fun release() {
        playerView.playerView.player = null
        player.release()
    }

}