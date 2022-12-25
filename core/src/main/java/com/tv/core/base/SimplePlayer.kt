package com.tv.core.base

import android.content.Context
import com.google.android.exoplayer2.ui.PlayerView
import com.tv.core.ui.TvPlayerView

class SimplePlayer(
    context: Context,
    private val playerView: TvPlayerView,
    playWhenReady: Boolean = true
) : BasePlayer(context, playerView, playWhenReady){

    override fun release() {
        playerView.playerView.player = null
        player.release()
    }

}