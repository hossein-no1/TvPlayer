package com.tv.core.base

import androidx.appcompat.app.AppCompatActivity
import com.tv.core.ui.TvPlayerView

internal class SimplePlayer(
    private val tvPlayerView: TvPlayerView,
    activity: AppCompatActivity,
    isLive: Boolean = false,
    playWhenReady: Boolean = true
) : TvPlayer(activity, tvPlayerView, isLive, playWhenReady) {

    override fun release() {
        tvPlayerView.playerView.player = null
        player.release()
    }

}