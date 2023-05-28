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
) : TvPlayer(activity, tvPlayerView, isLive, playWhenReady, tvImaAdsLoader) {

    override fun release() {
        tvPlayerView.playerView.player = null
        player.release()
        tvImaAdsLoader?.release()
    }

}