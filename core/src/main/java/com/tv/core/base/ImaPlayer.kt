package com.tv.core.base

import androidx.appcompat.app.AppCompatActivity
import com.tv.core.ui.TvPlayerView
import com.tv.core.util.TvImaAdsLoader

internal class ImaPlayer(
    private val tvPlayerView: TvPlayerView,
    private val tvImaAdsLoader: TvImaAdsLoader? = null,
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
    tvImaAdsLoader = tvImaAdsLoader,
    minBufferMs = minBufferMs,
    maxBufferMs = maxBufferMs
) {

    override fun release() {
        tvPlayerView.playerView.player = null
        player.release()
        tvImaAdsLoader?.release()
    }

}