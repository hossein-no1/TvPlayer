package com.tv.core.base

import android.app.Activity
import com.tv.core.ui.TvPlayerView
import com.tv.core.util.TvImaAdsLoader

internal class ImaPlayer(
    private val tvPlayerView: TvPlayerView,
    private val tvImaAdsLoader: TvImaAdsLoader? = null,
    activity: Activity,
    isLive: Boolean = false,
    playWhenReady: Boolean = true,
) : TvPlayer(activity, tvPlayerView, isLive, playWhenReady, tvImaAdsLoader) {

    override fun release() {
        tvPlayerView.playerView.player = null
        player.release()
        tvImaAdsLoader?.release()
    }

}