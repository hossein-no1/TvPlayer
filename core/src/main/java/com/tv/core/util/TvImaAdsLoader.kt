package com.tv.core.util

import android.content.Context
import com.google.ads.interactivemedia.v3.api.AdErrorEvent.AdErrorListener
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer.VideoAdPlayerCallback
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader


class TvImaAdsLoader(
    context: Context
) {

    private val imaAdsLoaderBuilder: ImaAdsLoader.Builder = ImaAdsLoader.Builder(context)
    private val imaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings()
    internal var imaAdsLoader: ImaAdsLoader? = null

    fun setAdErrorListener(adErrorListener: AdErrorListener): TvImaAdsLoader {
        imaAdsLoaderBuilder.setAdErrorListener(adErrorListener)
        return this
    }

    fun setAdEventListener(adEventListener: AdEventListener): TvImaAdsLoader {
        imaAdsLoaderBuilder.setAdEventListener(adEventListener)
        return this
    }

    fun setVideoAdPlayerCallBack(videoAdPlayerCallback: VideoAdPlayerCallback): TvImaAdsLoader {
        imaAdsLoaderBuilder.setVideoAdPlayerCallback(videoAdPlayerCallback)
        return this
    }

    fun setFocusSkipButtonWhenAvailable(focusSkipButtonWhenAvailable: Boolean): TvImaAdsLoader {
        imaAdsLoaderBuilder.setFocusSkipButtonWhenAvailable(focusSkipButtonWhenAvailable)
        return this
    }

    fun setVastLoadTimeoutMs(timeOutMs: Int): TvImaAdsLoader {
        imaAdsLoaderBuilder.setVastLoadTimeoutMs(timeOutMs)
        return this
    }

    fun setMediaLoadTimeoutMs(timeOutMs: Int): TvImaAdsLoader {
        imaAdsLoaderBuilder.setMediaLoadTimeoutMs(timeOutMs)
        return this
    }

    fun setPerLoadTimeoutMs(timeOutMs: Long): TvImaAdsLoader {
        imaAdsLoaderBuilder.setAdPreloadTimeoutMs(timeOutMs)
        return this
    }

    fun skipAd() {
        imaAdsLoader?.skipAd()
    }

    fun focusSkipButton() {
        imaAdsLoader?.focusSkipButton()
    }

    fun setLanguage(language : String = "fa") : TvImaAdsLoader {
        imaSdkSettings.language = language
        return this
    }

    internal fun setPlayer(player: Player?) {
        imaAdsLoader?.setPlayer(player)
    }

    internal fun release() {
        imaAdsLoader?.release()
    }

    fun create(): TvImaAdsLoader {
        imaAdsLoader = imaAdsLoaderBuilder
            .setImaSdkSettings(imaSdkSettings)
            .build()
        return this
    }

}