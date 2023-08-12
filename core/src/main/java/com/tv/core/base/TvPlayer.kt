package com.tv.core.base

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.Listener
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Util
import com.tv.core.R
import com.tv.core.ui.BaseTvPlayerView
import com.tv.core.ui.TvAdvertisePlayerView
import com.tv.core.ui.TvPlayerView
import com.tv.core.util.AdvertisePlayerListener
import com.tv.core.util.ExoPlayerHelper
import com.tv.core.util.TvImaAdsLoader
import com.tv.core.util.TvPlayBackException
import com.tv.core.util.TvPlayerListener
import com.tv.core.util.mediaItems.AdvertiseItem
import com.tv.core.util.mediaItems.DubbedItem
import com.tv.core.util.mediaItems.EpisodeMediaItem
import com.tv.core.util.mediaItems.MediaItemConverter
import com.tv.core.util.mediaItems.MediaItemParent
import com.tv.core.util.mediaItems.SubtitleConverter
import com.tv.core.util.ui.AlertDialogHelper
import com.tv.core.util.ui.AlertDialogItemView
import java.util.Formatter
import java.util.Locale

abstract class TvPlayer(
    val activity: AppCompatActivity,
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

    val currentMediaItem: MediaItemParent
        get() {
            return mediaItems[player.currentMediaItemIndex]
        }
    val mediaItems = mutableListOf<MediaItemParent>()

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

    fun getCurrentQuality() = currentMediaItem.currentQuality

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
                    if (startToPlayMedia && !isAdPlaying()) {
                        listener.onMediaStartToPlay(currentMediaItem)
                        startToPlayMedia = false
                    }
                    tvPlayerView.changeSubtitleState(isThereSubtitle())
                    tvPlayerView.changeQualityState(isThereQualities())
                    tvPlayerView.changeAudioTrackState(isThereDubbed())
                } else if (playbackState == STATE_ENDED && !isAdPlaying()) {
                    listener.onMediaListComplete(currentMediaItem)
                    startToPlayMedia = true
                }
                tvPlayerView.changeEpisodeListState(isThereEpisodeMediaItem())
                listener.onPlaybackStateChanged(playbackState)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION && isRealMediaComplete(
                        oldPosition.positionMs
                    )
                ) {
                    listener.onMediaComplete(mediaItems[oldPosition.mediaItemIndex])
                    startToPlayMedia = true
                } else if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT && !isAdPlaying()) {
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
        tvPlayerView.playerView.setControllerVisibilityListener(StyledPlayerView.ControllerVisibilityListener { visibility ->
            listener.onControllerVisibilityChanged(visibility)
        })
        player.addListener(requireNotNull(playerListener))
    }

    private fun isRealMediaComplete(position: Long): Boolean {
        val mediaDuration = getDuration() - 1_000
        return mediaDuration > 0 && (position >= mediaDuration)
    }

    fun addMedia(media: MediaItemParent, index: Int = 0) {
        mediaItems.add(media)
        player.addMediaSource(index, buildMediaSource(media, media.dubbedList))
    }

    fun addMediaList(medias: List<MediaItemParent>, index: Int = 0) {
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

    fun isThereEpisodeMediaItem() = mediaItems.any { it is EpisodeMediaItem }

    fun isThereQualities() = currentMediaItem.isThereQuality()

    private fun buildMediaSource(mediaItem: MediaItemParent, dubbedList: List<DubbedItem>) =
        MergingMediaSource(
            mediaSourceFactory.createMediaSource(MediaItemConverter.convertMediaItem(mediaItem)),
            *buildDubbedMediaSource(dubbedList).toTypedArray(),
            *buildSubtitleMediaSource(mediaItem).toTypedArray()
        )

    private fun buildMediaSources(mediaItems: List<MediaItemParent>): List<MediaSource> {
        val mediaSources = mutableListOf<MediaSource>()
        mediaItems.forEach { mediaItem ->
            mediaSources.add(buildMediaSource(mediaItem, mediaItem.dubbedList))
        }
        return mediaSources
    }

    private fun buildSubtitleMediaSource(mediaItem: MediaItemParent): ArrayList<MediaSource> {
        val subtitleSources: ArrayList<MediaSource> = arrayListOf()
        mediaItem.subtitleItems.forEach { subtitleConfig ->
            subtitleSources.add(
                SingleSampleMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(SubtitleConverter.convert(subtitleConfig), C.TIME_UNSET)
            )
        }
        return subtitleSources
    }

    private fun buildDubbedMediaSource(dubbedList: List<DubbedItem>): ArrayList<MediaSource> {
        val dubbedSources: ArrayList<MediaSource> = arrayListOf()
        dubbedList.forEach { dubbedMedia ->
            dubbedSources.add(
                DefaultMediaSourceFactory(activity).createMediaSource(
                    MediaItem.Builder()
                        .setUri(dubbedMedia.url)
                        .build()
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

    fun seekTo(msSecond: Long) {
        player.seekTo(msSecond)
    }

    fun changeMedia(index: Int, seekPosition: Long = 0L) {
        player.seekTo(index, seekPosition)
    }

    internal fun showSubtitle(
        dialogTitle: String, dialogButtonText: String, resIdStyle: Int
    ) {
        val subtitleLanguageList = ArrayList<String>()
        val subtitlesList = ArrayList<AlertDialogItemView>()

        player.currentTracks.groups.forEach { group ->
            if (group.type == C.TRACK_TYPE_TEXT) {
                val groupInfo = group.mediaTrackGroup

                subtitleLanguageList.add(groupInfo.getFormat(0).language.toString())
                val subtitleText =
                    "${subtitlesList.size + 1}. " + Locale(groupInfo.getFormat(0).language.toString()).displayLanguage + " (${
                        if (groupInfo.getFormat(0).label == null) "Subtitle" else groupInfo.getFormat(
                            0
                        ).label
                    })"

                val subtitleIcon = if (group.isSelected) R.drawable.tv_ic_check else 0
                val subtitleCheckSupported = group.isSupported
                subtitlesList.add(
                    AlertDialogItemView(
                        subtitleText, subtitleIcon, subtitleCheckSupported
                    )
                )

            }
        }

        val subtitleDialog = AlertDialogHelper(activity, resIdStyle, dialogTitle)
        subtitleDialog.create(
            adapter = getAlertDialogAdapter(subtitlesList.toTypedArray()),
            itemClickListener = { _, position ->
                selectSubtitle(position)
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

    internal fun showAudioTrack(
        dialogTitle: String, dialogButtonText: String, resIdStyle: Int
    ) {
        val audioTrackLanguageList = ArrayList<String>()
        val audioTracksList = ArrayList<AlertDialogItemView>()

        var softAudioTrackCounter = 0
        val allAudioMediaSize =
            player.currentTracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }.size
        val softAudioMediaSize = currentMediaItem.dubbedList.size
        val hardAudioMediaSize = allAudioMediaSize - softAudioMediaSize

        player.currentTracks.groups.forEachIndexed { index, group ->
            if (group.type == C.TRACK_TYPE_AUDIO) {
                val groupInfo = group.mediaTrackGroup
                audioTrackLanguageList.add(groupInfo.getFormat(0).language.toString())
                val displayLanguage =
                    Locale(groupInfo.getFormat(0).language.toString()).displayLanguage.let {
                        if (it == "null") "" else it
                    }
                var audioTrackText = "${audioTracksList.size + 1}. " + displayLanguage + " (${
                    if (groupInfo.getFormat(0).label == null) "Dubbed" else groupInfo.getFormat(0).label
                })"

                if (index > hardAudioMediaSize) {
                    audioTrackText = "${audioTracksList.size + 1}. " + currentMediaItem.dubbedList[softAudioTrackCounter++].title
                }

                val audioTrackIcon = if (group.isSelected) R.drawable.tv_ic_check else 0
                val audioTrackCheckSupported = group.isSupported
                audioTracksList.add(
                    AlertDialogItemView(
                        audioTrackText, audioTrackIcon, audioTrackCheckSupported
                    )
                )

            }
        }

        val audioTrackDialog = AlertDialogHelper(activity, resIdStyle, dialogTitle)
        audioTrackDialog.create(
            adapter = getAlertDialogAdapter(audioTracksList.toTypedArray()),
            itemClickListener = { _, position ->
                selectAudioTrack(position)
            },
            positiveClickListener = { self, _ ->
                self.dismiss()
            },
            positiveButtonText = dialogButtonText
        )
        audioTrackDialog.show()
    }

    fun selectSubtitle(position: Int) {
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
    }

    fun selectAudioTrack(position: Int) {
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
    }

    internal fun showQuality(
        dialogTitle: String, dialogButtonText: String, resIdStyle: Int
    ) {
        val qualityList = ArrayList<AlertDialogItemView>()

        currentMediaItem.qualityList.forEach { mediaQuality ->
            qualityList.add(
                AlertDialogItemView(
                    mediaQuality.title, if (mediaQuality.isSelected) R.drawable.tv_ic_check else 0
                )
            )
        }

        val qualityDialog = AlertDialogHelper(activity, resIdStyle, dialogTitle)
        qualityDialog.create(adapter = getAlertDialogAdapter(qualityList.toTypedArray()),
            itemClickListener = { self, position ->
                if (!currentMediaItem.qualityList[position].isSelected) changeQuality(position)
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
        val mediaSource = buildMediaSource(currentMediaItem.changeQualityUriInItem(
            qualitySelectedPosition
        ), currentMediaItem.dubbedList)
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
                tv.setTextColor(
                    if (items[position].isSupported) Color.parseColor("#ffffff") else Color.parseColor(
                        "#F44336"
                    )
                )
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0)

                val dp5 = (5 * context.resources.displayMetrics.density + 0.5f).toInt()
                tv.compoundDrawablePadding = dp5

                return v
            }
        }
    }

    class Builder(
        private val activity: AppCompatActivity,
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