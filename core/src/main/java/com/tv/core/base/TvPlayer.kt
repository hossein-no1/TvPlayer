package com.tv.core.base

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.TextView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.Listener
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Util
import com.tv.core.R
import com.tv.core.ui.BaseTvPlayerView
import com.tv.core.ui.TvAdvertisePlayerView
import com.tv.core.ui.TvPlayerView
import com.tv.core.util.*
import java.util.*
import com.tv.core.util.MediaItem as TvMediaItem

abstract class TvPlayer(
    private val activity: Activity,
    private val tvPlayerView: BaseTvPlayerView,
    isLive: Boolean = false,
    playWhenReady: Boolean = true,
    tvImaAdsLoader: TvImaAdsLoader? = null
) {

    companion object {
        const val STATE_IDLE = Player.STATE_IDLE // 1
        const val STATE_BUFFERING = Player.STATE_BUFFERING // 2
        const val STATE_READY = Player.STATE_READY // 3
        const val STATE_ENDED = Player.STATE_ENDED // 4
    }

    private var trackSelector: DefaultTrackSelector
    private var playerListener: Listener? = null
    var player: ExoPlayer
    private lateinit var mediaSourceFactory: MediaSource.Factory
    private var dataSourceFactory: DataSource.Factory

    val currentMediaItem: TvMediaItem
        get() {
            return mediaItems[player.currentMediaItemIndex]
        }
    private val mediaItems = mutableListOf<TvMediaItem>()

    private var startToPlayMedia = false

    private val formatBuilder = StringBuilder()
    private val formatter = Formatter(formatBuilder, Locale.getDefault())

    init {
        setupElement(isLive)
        trackSelector = ExoPlayerHelper.getTrackSelector(activity.applicationContext)
        dataSourceFactory = ExoPlayerHelper.getDataSourceFactory(activity)
        tvImaAdsLoader?.let { safeAdsLoader ->
            mediaSourceFactory = ExoPlayerHelper.getMediaSourceFactory(
                activity = activity,
                dataSourceFactory = dataSourceFactory,
                tvImaAdsLoader = safeAdsLoader,
                playerView = tvPlayerView.playerView
            )
        } ?: kotlin.run {
            mediaSourceFactory = ExoPlayerHelper.getMediaSourceFactory(
                activity = activity,
                dataSourceFactory = dataSourceFactory,
            )
        }

        player = ExoPlayerHelper.getExoPlayer(
            context = activity.applicationContext,
            trackSelector = trackSelector,
            mediaSourceFactory = mediaSourceFactory
        )
        tvPlayerView.playerView.player = player
        tvImaAdsLoader?.setPlayer(player)
        player.playWhenReady = playWhenReady
    }

    private fun setupElement(isLive: Boolean) {
        tvPlayerView.setupElement(this, isLive)
    }

    fun isPlaying() = player.isPlaying

    fun isAdPlaying() = player.isPlayingAd

    fun getCurrentQuality() = currentMediaItem.getCurrentQuality()

    fun addListener(listener: TvPlayerListener) {
        //Remove last listener
        playerListener?.let { safeListener ->
            player.removeListener(safeListener)
        }
        playerListener = object : Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                listener.onPlayerError(
                    TvPlayBackException(
                        errorMessage = error.message, errorCode = error.errorCode
                    )
                )
                startToPlayMedia = true
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == STATE_READY) {
                    if (startToPlayMedia) {
                        listener.onMediaStartToPlay(currentMediaItem)
                        startToPlayMedia = false
                    }
                    tvPlayerView.changeSubtitleState(isThereSubtitle())
                    tvPlayerView.changeQualityState(isThereQualities())
                    tvPlayerView.changeAudioTrackState(isThereDubbed())
                } else if (playbackState == STATE_ENDED) {
                    listener.onMediaListComplete(currentMediaItem)
                    startToPlayMedia = true
                }
                listener.onPlaybackStateChanged(playbackState)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    listener.onMediaComplete(currentMediaItem)
                    startToPlayMedia = true
                } else if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
                    listener.onMediaChange(currentMediaItem)
                    startToPlayMedia = true
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                super.onTracksChanged(tracks)
                if (getCurrentPosition() == 0L) player.seekTo(
                    currentMediaItem.startPositionMs
                )
            }

        }
        player.addListener(requireNotNull(playerListener))
    }

    fun addMedia(media: TvMediaItem, index: Int = 0) {
        mediaItems.add(media)
        player.addMediaSource(
            index, buildMediaSource(
                MediaItemConverter.convertMediaItem(media), media.dubbedList
            )
        )
    }

    fun addMediaList(medias: List<TvMediaItem>, index: Int = 0) {
        mediaItems.addAll(medias)
        player.addMediaSources(
            index, buildMediaSources(medias)
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

    fun isThereDubbed(): Boolean {
        for (group in player.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                return true
            }
        }
        return false
    }

    fun isThereQualities() = currentMediaItem.isThereQuality()

    private fun buildMediaSource(mediaItem: MediaItem, dubbedList: List<String>) =
        MergingMediaSource(
            mediaSourceFactory.createMediaSource(mediaItem),
            *buildDubbedMediaSource(dubbedList).toTypedArray(),
            *buildSubtitleMediaSource(mediaItem).toTypedArray()
        )

    private fun buildMediaSources(mediaItems: List<com.tv.core.util.MediaItem>): List<MediaSource> {
        val mediaSources = mutableListOf<MediaSource>()
        mediaItems.forEach { mediaItem ->
            mediaSources.add(
                buildMediaSource(
                    MediaItemConverter.convertMediaItem(mediaItem), mediaItem.dubbedList
                )
            )
        }
        return mediaSources
    }

    private fun buildSubtitleMediaSource(mediaItem: MediaItem): ArrayList<MediaSource> {
        val subtitleSources: ArrayList<MediaSource> = arrayListOf()
        mediaItem.localConfiguration?.subtitleConfigurations?.forEach { subtitleConfig ->
            subtitleSources.add(
                SingleSampleMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(subtitleConfig, C.TIME_UNSET)
            )
        }
        return subtitleSources
    }

    private fun buildDubbedMediaSource(dubbedList: List<String>): ArrayList<MediaSource> {
        val dubbedSources: ArrayList<MediaSource> = arrayListOf()
        dubbedList.forEach {
            dubbedSources.add(
                DefaultMediaSourceFactory(activity).createMediaSource(
                    MediaItem.Builder().setUri(
                        it
                    ).build()
                )
            )
        }
        return dubbedSources
    }

    fun prepare() {
        player.prepare()
    }

    fun prepareAndPlay(mediaIndex: Int = 0, mediaSeek: Long = C.TIME_UNSET) {
        prepare()
        play(mediaIndex, mediaSeek)
    }

    fun play(mediaIndex: Int = 0, mediaSeek: Long = C.TIME_UNSET) {
        player.seekTo(mediaIndex, mediaSeek)
        player.play()
        startToPlayMedia = true
    }

    fun stop() {
        player.stop()
    }

    fun pause() {
        player.pause()
    }

    fun getCurrentPosition() = player.currentPosition

    fun getDuration() = player.duration

    fun getCurrentPositionString() =
        Util.getStringForTime(formatBuilder, formatter, player.currentPosition)

    fun getPositionString(position: Long) =
        Util.getStringForTime(formatBuilder, formatter, if (position > 0) position else 0)

    fun getDurationString() = Util.getStringForTime(formatBuilder, formatter, player.duration)

    fun fastForwardIncrement(duration: Int = 10) {
        val length = if (duration <= 1_000) duration * 1_000 else duration
        player.seekTo(player.currentPosition + length)
    }

    fun fastRewindIncrement(duration: Int = 10) {
        val length = if (duration <= 1_000) duration * 1_000 else duration
        player.seekTo(player.currentPosition - length)
    }

    fun showSubtitle(
        dialogTitle: String, dialogButtonText: String, resIdStyle: Int
    ) {
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

        val subtitleDialog = AlertDialogHelper(activity, resIdStyle, dialogTitle)
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
            positiveButtonText = dialogButtonText
        )
        subtitleDialog.show()
    }

    fun showAudioTrack(
        dialogTitle: String, dialogButtonText: String, resIdStyle: Int
    ) {
        val audioTrackLanguageList = ArrayList<String>()
        val audioTracksList = ArrayList<AlertDialogItemView>()
        for (group in player.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                val groupInfo = group.mediaTrackGroup
                for (i in 0 until groupInfo.length) {
                    audioTrackLanguageList.add(groupInfo.getFormat(i).language.toString())
                    val displayLanguage =
                        Locale(groupInfo.getFormat(i).language.toString()).displayLanguage.let {
                            if (it == "null") "" else it
                        }
                    val subtitleText = "${audioTracksList.size + 1}. " + displayLanguage + " (${
                        if (groupInfo.getFormat(
                                i
                            ).label == null
                        ) "Dubbed" else groupInfo.getFormat(i).label
                    })"
                    val audioTrackIcon = if (group.isSelected) R.drawable.ic_check else 0
                    audioTracksList.add(AlertDialogItemView(subtitleText, audioTrackIcon))
                }
            }
        }

        val audioTrackDialog = AlertDialogHelper(activity, resIdStyle, dialogTitle)
        audioTrackDialog.create(
            adapter = getAlertDialogAdapter(audioTracksList.toTypedArray()),
            itemClickListener = { _, position ->
                val trackGroupList =
                    trackSelector.currentMappedTrackInfo?.getTrackGroups(C.TRACK_TYPE_AUDIO)
                val trackGroup = trackGroupList?.get(position)
                trackGroup?.let { safeTrackGroup ->
                    trackSelector.setParameters(
                        trackSelector.buildUponParameters().setOverrideForType(
                            TrackSelectionOverride(
                                safeTrackGroup, 0
                            )
                        ).setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
                    )
                }
            },
            positiveClickListener = { self, _ ->
                self.dismiss()
            },
            positiveButtonText = dialogButtonText
        )
        audioTrackDialog.show()
    }

    fun showQuality(
        dialogTitle: String, dialogButtonText: String, resIdStyle: Int
    ) {
        val qualityList = ArrayList<AlertDialogItemView>()

        currentMediaItem.getQualityList().forEach { mediaQuality ->
            qualityList.add(
                AlertDialogItemView(
                    mediaQuality.title, if (mediaQuality.isSelected) R.drawable.ic_check else 0
                )
            )
        }

        val qualityDialog = AlertDialogHelper(activity, resIdStyle, dialogTitle)
        qualityDialog.create(adapter = getAlertDialogAdapter(qualityList.toTypedArray()),
            itemClickListener = { self, position ->
                if (!currentMediaItem.getQualityList()[position].isSelected) changeQuality(position)
                else self.dismiss()
            },
            positiveButtonText = dialogButtonText,
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

    fun updateMediaStartPosition(positionMs: Long) {
        currentMediaItem.startPositionMs = positionMs
    }

    private fun changeQualityUriInItem(qualitySelectedPosition: Int) {
        val mediaSource = buildMediaSource(
            MediaItemConverter.convertMediaItem(
                currentMediaItem.changeQualityUriInItem(
                    qualitySelectedPosition
                )
            ), currentMediaItem.dubbedList
        )
        player.setMediaSource(mediaSource)
    }

    private fun changeQualityUriInMediaList(qualitySelectedPosition: Int) {
        mediaItems[player.currentMediaItemIndex].changeQualityUriInItem(qualitySelectedPosition)

        val mediaSources = buildMediaSources(mediaItems)
        player.setMediaSources(mediaSources, player.currentMediaItemIndex, player.currentPosition)
    }

    private fun getAlertDialogAdapter(items: Array<AlertDialogItemView>): ListAdapter {
        return object : ArrayAdapter<AlertDialogItemView>(
            activity.applicationContext,
            android.R.layout.select_dialog_item,
            android.R.id.text1,
            items
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
        private val activity: Activity,
        private val playerView: TvPlayerView,
        private val playWhenReady: Boolean = true,
    ) {

        fun createSimplePlayer(
            isLive: Boolean = false
        ): TvPlayer = SimplePlayer(
            activity = activity,
            tvPlayerView = playerView,
            isLive = isLive,
            playWhenReady = playWhenReady
        )

        fun createImaPlayer(
            tvImaAdsLoader: TvImaAdsLoader? = null, isLive: Boolean = false
        ): TvPlayer = ImaPlayer(
            activity = activity,
            tvPlayerView = playerView,
            isLive = isLive,
            playWhenReady = playWhenReady,
            tvImaAdsLoader = tvImaAdsLoader
        )

        fun createAdvertisePlayer(
            adPlayerView: TvAdvertisePlayerView, isLive: Boolean = false
        ): TvPlayer = AdvertisePlayer(
            activity = activity,
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