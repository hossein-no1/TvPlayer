package com.tv.core.base

import android.content.Context
import com.google.android.exoplayer2.ui.PlayerView

class SimplePlayer(
    context: Context,
    private val playerView: PlayerView,
    playWhenReady: Boolean = true
) : BasePlayer(context, playerView, playWhenReady){

    override fun release() {
        playerView.player = null
        player.release()
    }

}