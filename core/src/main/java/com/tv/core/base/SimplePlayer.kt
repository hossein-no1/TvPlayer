package com.tv.core.base

import androidx.appcompat.app.AppCompatActivity
import com.tv.core.ui.TvPlayerView

internal class SimplePlayer(
    private val tvPlayerView: TvPlayerView,
    activity: AppCompatActivity,
    isLive: Boolean = false,
    playWhenReady: Boolean = true,
    minBufferMs: Int,
    maxBufferMs: Int
) : TvPlayer(
    activity = activity,
    tvPlayerView = tvPlayerView,
    isLive = isLive,
    playWhenReady = playWhenReady,
    minBufferMs = minBufferMs,
    maxBufferMs = maxBufferMs
) {

    override fun release() {
        tvPlayerView.playerView.player = null
        player.release()
    }

}