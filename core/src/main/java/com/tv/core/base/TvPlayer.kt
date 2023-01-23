package com.tv.core.base

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.TextView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.Listener
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.tv.core.R
import com.tv.core.ui.BaseTvPlayerView
import com.tv.core.ui.TvAdvertisePlayerView
import com.tv.core.ui.TvPlayerView
import com.tv.core.util.*
import java.util.*
import com.tv.core.util.MediaItem as TvMediaItem

abstract class TvPlayer(
    private val context: Context,
    private val tvPlayerView: BaseTvPlayerView,
    isLive: Boolean = false,
    playWhenReady: Boolean = true
) {

    companion object {
        const val STATE_IDLE = Player.STATE_IDLE
        const val STATE_BUFFERING = Player.STATE_BUFFERING
        const val STATE_READY = Player.STATE_READY
        const val STATE_ENDED = Player.STATE_ENDED
    }

    private var trackSelector: DefaultTrackSelector
    private var playerListener: Listener? = null
    var player: ExoPlayer

    private val currentMediaItem: TvMediaItem
        get() {
            return mediaItems[player.currentMediaItemIndex]
        }
    private val mediaItems = mutableListOf<TvMediaItem>()

    init {
        setupElement(isLive)
        val videoTrackSelectionFactory =
            AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(context.applicationContext, videoTrackSelectionFactory)
        player = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()
        tvPlayerView.playerView.player = player
        player.playWhenReady = playWhenReady
    }

    private fun setupElement(isLive: Boolean) {
        tvPlayerView.setupElement(this, isLive)
    }

    fun isPlaying() = player.isPlaying

    fun addListener(listener: TvPlayerListener) {
        //Remove last listener
        playerListener?.let { safeListener ->
            player.removeListener(safeListener)
        }
        playerListener = object : Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                listener.onPlayerError(error)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_READY) {
                    tvPlayerView.changeSubtitleState(isThereSubtitle())
                    tvPlayerView.changeQualityState(isThereQualities())
                }
                listener.onPlaybackStateChanged(playbackState)
            }

        }
        player.addListener(requireNotNull(playerListener))
    }

    fun addMedia(media: TvMediaItem, index: Int = 0) {
        mediaItems.add(media)
        player.addMediaSource(index, buildMediaSource(MediaItemConverter.convertMediaItem(media)))
    }

    fun addMediaList(medias: List<TvMediaItem>, index: Int = 0) {
        mediaItems.addAll(medias)
        player.addMediaSources(
            index,
            buildMediaSources(MediaItemConverter.convertMediaList(medias))
        )
    }

    fun isThereSubtitle(): Boolean {
        for (group in player.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                return true
            }
        }
        return false
    }

    fun isThereQualities() = currentMediaItem.isThereQuality()

    private fun buildMediaSources(mediaItems: List<MediaItem>): List<MediaSource> {
        val mediaSources = mutableListOf<MediaSource>()
        mediaItems.forEach { mediaItem ->
            mediaSources.add(buildMediaSource(mediaItem))
        }
        return mediaSources
    }

    private fun buildMediaSource(mediaItem: MediaItem): MediaSource {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(Util.getUserAgent(context, context.packageName))
        val mediaSource: MediaSource =
            if (mediaItem.localConfiguration?.tag.toString() == MediaSourceType.Hls.name)
                HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem) else ProgressiveMediaSource.Factory(
                dataSourceFactory
            ).createMediaSource(mediaItem)
        val subtitleSources: ArrayList<MediaSource> = arrayListOf()

        mediaItem.localConfiguration?.subtitleConfigurations?.forEach { subtitleConfig ->
            subtitleSources.add(
                SingleSampleMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(subtitleConfig, C.TIME_UNSET)
            )
        }
        return MergingMediaSource(mediaSource, *subtitleSources.toTypedArray())
    }

    fun prepare() {
        player.prepare()
    }

    fun prepareAndPlay() {
        prepare()
        play()
    }

    fun play() {
        player.play()
    }

    fun stop() {
        player.stop()
    }

    fun pause() {
        player.pause()
    }

    fun fastForwardIncrement(duration: Int = 10) {
        val length = if (duration <= 1_000) duration * 1_000 else duration
        player.seekTo(player.currentPosition + length)
    }

    fun fastRewindIncrement(duration: Int = 10) {
        val length = if (duration <= 1_000) duration * 1_000 else duration
        player.seekTo(player.currentPosition - length)
    }

    fun showSubtitle(overrideThemeResId: Int = R.style.defaultAlertDialogStyle) {
        val subtitleLanguageList = ArrayList<String>()
        val subtitlesList = ArrayList<AlertDialogItemView>()
        for (group in player.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                val groupInfo = group.mediaTrackGroup
                for (i in 0 until groupInfo.length) {
                    subtitleLanguageList.add(groupInfo.getFormat(i).language.toString())
                    val subtitleText =
                        "${subtitlesList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage + " (${
                            if (groupInfo.getFormat(
                                    i
                                ).label == null
                            ) "Subtitle" else groupInfo.getFormat(i).label
                        })"
                    val subtitleIcon = if (group.isSelected) R.drawable.ic_check else 0
                    subtitlesList.add(AlertDialogItemView(subtitleText, subtitleIcon))
                }
            }
        }

        val subtitleDialog = AlertDialogHelper(context, overrideThemeResId, "Select subtitles")
        subtitleDialog.create(
            adapter = getAlertDialogAdapter(subtitlesList.toTypedArray()),
            itemClickListener = { _, position ->
                val trackGroupList =
                    trackSelector.currentMappedTrackInfo?.getTrackGroups(C.TRACK_TYPE_VIDEO)
                val trackGroup = trackGroupList?.get(position)
                trackGroup?.let { safeTrackGroup ->
                    trackSelector.setParameters(
                        trackSelector.buildUponParameters().setOverrideForType(
                            TrackSelectionOverride(
                                safeTrackGroup, 0
                            )
                        ).setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                    )
                }
            },
            positiveClickListener = { self, _ ->
                trackSelector.setParameters(
                    trackSelector.buildUponParameters().setRendererDisabled(
                        C.TRACK_TYPE_VIDEO, true
                    )
                )
                self.dismiss()
            },
            positiveButtonText = "Off Subtitles"
        )
        subtitleDialog.show()
    }

    fun showQuality(overrideThemeResId: Int = R.style.defaultAlertDialogStyle) {
        val qualityList = ArrayList<AlertDialogItemView>()

        currentMediaItem.getQualityList().forEach { mediaQuality ->
            qualityList.add(
                AlertDialogItemView(
                    mediaQuality.title,
                    if (mediaQuality.isSelected) R.drawable.ic_check else 0
                )
            )
        }

        val qualityDialog = AlertDialogHelper(context, overrideThemeResId, "Select quality")
        qualityDialog.create(adapter = getAlertDialogAdapter(qualityList.toTypedArray()),
            itemClickListener = { self, position ->
                if (!currentMediaItem.getQualityList()[position].isSelected)
                    changeQuality(position)
                else
                    self.dismiss()
            },
            positiveButtonText = "Close",
            positiveClickListener = { self, _ ->
                self.dismiss()
            })
        qualityDialog.show()
    }

    private fun changeQuality(qualitySelectedPosition: Int) {
        val currentTime = player.currentPosition
        if (mediaItems.size > 1) changeQualityUriInMediaList(qualitySelectedPosition) else changeQualityUriInItem(
            qualitySelectedPosition
        )
        player.prepare()
        player.seekTo(currentTime)
    }

    private fun changeQualityUriInItem(qualitySelectedPosition: Int) {
        currentMediaItem.url =
            currentMediaItem.getQualityList()[qualitySelectedPosition].link

        val mediaSource =
            buildMediaSource(MediaItemConverter.convertMediaItem(currentMediaItem))
        player.setMediaSource(mediaSource)
    }

    private fun changeQualityUriInMediaList(qualitySelectedPosition: Int) {
        resetQualitySelected()
        currentMediaItem.getQualityList()[qualitySelectedPosition].let {
            mediaItems[player.currentMediaItemIndex].url = it.link
            it.isSelected = true
        }

        val mediaSources = buildMediaSources(
            MediaItemConverter.convertMediaList(
                mediaItems
            )
        )
        player.setMediaSources(mediaSources, player.currentMediaItemIndex, player.currentPosition)
    }

    private fun resetQualitySelected() {
        currentMediaItem.getQualityList().forEach { mediaQuality ->
            mediaQuality.isSelected = false
        }
    }

    private fun getAlertDialogAdapter(items: Array<AlertDialogItemView>): ListAdapter {
        return object : ArrayAdapter<AlertDialogItemView>(
            context, android.R.layout.select_dialog_item, android.R.id.text1, items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v: View = super.getView(position, convertView, parent)
                val tv = v.findViewById<TextView>(android.R.id.text1)

                tv.text = items[position].text
                tv.textSize = 16F
                tv.setTextColor(Color.parseColor("#ffffff"))
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0)

                val dp5 = (5 * context.resources.displayMetrics.density + 0.5f).toInt()
                tv.compoundDrawablePadding = dp5

                return v
            }
        }
    }

    class Builder(
        private val context: Context,
        private val playerView: TvPlayerView,
        private val playWhenReady: Boolean = true
    ) {

        fun createSimplePlayer(isLive: Boolean = false): TvPlayer = SimplePlayer(
            context = context,
            tvPlayerView = playerView,
            isLive = isLive,
            playWhenReady = playWhenReady
        )

        fun createAdvertisePlayer(
            adPlayerView: TvAdvertisePlayerView,
            isLive: Boolean = false
        ): TvPlayer =
            AdvertisePlayer(
                context = context,
                tvPlayerView = playerView,
                tvAdvertisePlayerView = adPlayerView,
                isLive = isLive,
                playWhenReady = playWhenReady
            )

    }

    abstract fun release()
    open fun playAdvertiseAutomatic() {}
    open fun startForceVideo() {}
    open fun addMediaAdvertise(media: AdvertiseItem, skippTime: Int = 10, index: Int = 0) {}
    open fun playAdvertise() {}
    open fun stopAdvertise() {}
    open fun pauseAdvertise() {}
    open fun setAdvertiseListener(adListener: AdvertisePlayerListener) {}

}