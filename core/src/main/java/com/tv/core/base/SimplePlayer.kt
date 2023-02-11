package com.tv.core.base

import android.app.Activity
import com.tv.core.ui.TvPlayerView

internal class SimplePlayer(
    private val tvPlayerView: TvPlayerView,
    activity: Activity,
    isLive: Boolean = false,
    playWhenReady: Boolean = true
) : TvPlayer(activity, tvPlayerView, isLive, playWhenReady) {

    override fun release() {
        tvPlayerView.playerView.player = null
        player.release()
    }

}