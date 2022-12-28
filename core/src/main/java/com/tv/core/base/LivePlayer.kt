package com.tv.core.base

import android.content.Context
import com.tv.core.ui.TvLivePlayerView

class LivePlayer(
    context: Context,
    private val playerView: TvLivePlayerView,
    playWhenReady: Boolean = true
) : BasePlayer(context, playerView, playWhenReady){

    override fun release() {
        playerView.playerView.player = null
        player.release()
    }

}