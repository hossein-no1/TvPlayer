package com.tv.core.util

import android.app.Activity
import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.util.Util

object ExoPlayerHelper {

    fun getTrackSelector(context: Context) =
        DefaultTrackSelector(context, AdaptiveTrackSelection.Factory())

    fun getDataSourceFactory(activity: Activity) = DefaultHttpDataSource.Factory()
        .setUserAgent(Util.getUserAgent(activity, activity.packageName))

    fun getMediaSourceFactory(
        activity: Activity,
        dataSourceFactory: DataSource.Factory,
        tvImaAdsLoader: TvImaAdsLoader,
        playerView: PlayerView
    ) = DefaultMediaSourceFactory(activity)
        .setDataSourceFactory(CacheDataSource.Factory())
        .setDataSourceFactory(dataSourceFactory)
        .setLocalAdInsertionComponents(
            { tvImaAdsLoader.imaAdsLoader }, playerView
        )

    fun getMediaSourceFactory(
        activity: Activity,
        dataSourceFactory: DataSource.Factory,
    ) = DefaultMediaSourceFactory(activity)
        .setDataSourceFactory(CacheDataSource.Factory())
        .setDataSourceFactory(dataSourceFactory)

    fun getExoPlayer(
        context: Context,
        trackSelector: TrackSelector,
        mediaSourceFactory: MediaSource.Factory
    ) = ExoPlayer.Builder(context)
        .setTrackSelector(trackSelector)
        .setMediaSourceFactory(mediaSourceFactory)
        .setSeekBackIncrementMs(10_000)
        .setSeekForwardIncrementMs(10_000)
        .build()

}