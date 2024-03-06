package com.tv.core.base

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
import com.google.android.exoplayer2.source.TrackGroup
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
import com.tv.core.util.TVUserAction
import com.tv.core.util.TvImaAdsLoader
import com.tv.core.util.TvPlayBackException
import com.tv.core.util.TvPlayerInteractionListener
import com.tv.core.util.TvPlayerListener
import com.tv.core.util.mediaItems.AdvertiseItem
import com.tv.core.util.mediaItems.DubbedItem
import com.tv.core.util.mediaItems.EpisodeMediaItem
import com.tv.core.util.mediaItems.MediaItemConverter
import com.tv.core.util.mediaItems.MediaItemParent
import com.tv.core.util.mediaItems.SubtitleConverter
import com.tv.core.util.ui.AlertDialogHelper
import com.tv.core.util.ui.AlertDialogItemView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Formatter
import java.util.Locale
import kotlin.math.min

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
    private var interactionListener: TvPlayerInteractionListener? = null
    var player: ExoPlayer
    private lateinit var mediaSourceFactory: MediaSource.Factory
    private var dataSourceFactory: DataSource.Factory
    private var job: Job? = null

    val currentMediaItem: MediaItemParent
        get() {
            return mediaItems[player.currentMediaItemIndex]
        }
    val mediaItems = mutableListOf<MediaItemParent>()

    val currentMediaItemIndex
        get() = player.currentMediaItemIndex

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

    fun getCurrentQuality() = currentMediaItem.currentLink

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
                    ), currentMediaItem, currentMediaItemIndex
                )
                startToPlayMedia = true
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == STATE_READY) {
                    if (startToPlayMedia && !isAdPlaying()) {
                        listener.onMediaStartToPlay(currentMediaItem, currentMediaItemIndex)
                        startToPlayMedia = false
                    }
                    tvPlayerView.changeSourceState(isThereSource())
                    tvPlayerView.changeLinkState(isThereLink())
                    tvPlayerView.changeQualityState(isThereQuality())
                    tvPlayerView.changeAudioTrackState(isThereDubbed())
                    tvPlayerView.changeSubtitleState(isThereSubtitle())
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
                handleCheckMediaFinish()
                val hasAd = newPosition.adGroupIndex >= 0
                if (hasAd) {
                    listener.onAdRollStarted(
                        newPosition.adGroupIndex,
                        newPosition.adIndexInAdGroup
                    )
                }
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION && isRealMediaComplete(
                        oldPosition.positionMs
                    )
                ) {
                    if (!hasAd || newPosition.contentPositionMs == 0L)
                        listener.onMediaComplete(mediaItems[oldPosition.mediaItemIndex])
                    startToPlayMedia = true

                } else if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT && !isAdPlaying()) {
                    listener.onMediaChange(mediaItems[oldPosition.mediaItemIndex])
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

    fun handleCheckMediaFinish() {
        val timeToFinish = (getDuration() - getCurrentPosition())
        if (timeToFinish > 0 && player.hasNextMediaItem()) {
            job?.cancel()
            job = activity.lifecycleScope.launch {
                delay(timeToFinish)
                if (player.hasNextMediaItem()) {
                    player.seekToNextMediaItem()
                }
            }
        }
    }

    fun addInteractionListener(tvPlayerInteractionListener: TvPlayerInteractionListener) {
        interactionListener = tvPlayerInteractionListener
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

    fun updateMedia(index: Int, newMedia: MediaItemParent) {
        mediaItems[index] = newMedia
        player.removeMediaItem(index)
        player.addMediaSource(index, buildMediaSource(newMedia, newMedia.dubbedList))
    }

    fun isThereSubtitle(): Boolean {
        for (group in player.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                return true
            }
        }
        return false
    }

    fun isThereDubbed() = player.currentTracks.groups.count { it.type == C.TRACK_TYPE_AUDIO } > 1

    fun isThereQuality() =
        player.currentTracks.groups.count { it.type == C.TRACK_TYPE_VIDEO } > 0 && !isThereLink()

    fun isThereEpisodeMediaItem() = mediaItems.any { it is EpisodeMediaItem }

    fun isThereLink() = currentMediaItem.currentLink?.link?.contains(".m3u8") == false

    fun isThereSource() = currentMediaItem.isThereLink()

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

    fun seekToDefaultPosition() {
        player.seekToDefaultPosition()
    }

    fun changeMedia(index: Int, seekPosition: Long = 0L) {
        player.seekTo(index, seekPosition)
    }

    internal fun showSubtitle(
        dialogTitle: String,
        dialogButtonText: String,
        resIdStyle: Int,
        subtitleLanguageDictionary: Map<String?, String>,
        defaultSubtitle: String
    ) {
        interactionListener?.onUserAction(TVUserAction.SHOW_SUBTITLE)
        val subtitleLanguageList = ArrayList<String>()
        val subtitlesList = ArrayList<AlertDialogItemView>()

        player.currentTracks.groups.forEach { group ->
            if (group.type == C.TRACK_TYPE_TEXT) {
                val groupInfo = group.mediaTrackGroup

                subtitleLanguageList.add(groupInfo.getFormat(0).language.toString())
                val language = groupInfo.getFormat(0).language?.lowercase()
                val subtitleText =
                    subtitleLanguageDictionary[language] ?: language ?: defaultSubtitle

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
                interactionListener?.onUserAction(TVUserAction.SELECT_SUBTITLE)
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
        dialogTitle: String,
        dialogButtonText: String,
        resIdStyle: Int,
        audioLanguageDictionary: Map<String?, String>,
        defaultAudio: String
    ) {
        interactionListener?.onUserAction(TVUserAction.SHOW_AUDIO)

        val audioTrackLanguageList = ArrayList<String>()
        val audioTracksList = ArrayList<AlertDialogItemView>()

        player.currentTracks.groups.forEach { group ->
            if (group.type == C.TRACK_TYPE_AUDIO) {
                val groupInfo = group.mediaTrackGroup
                audioTrackLanguageList.add(groupInfo.getFormat(0).language.toString())
                val language = groupInfo.getFormat(0).language?.lowercase()
                val audioTrackText = audioLanguageDictionary[language] ?: language ?: defaultAudio

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
                interactionListener?.onUserAction(TVUserAction.SELECT_AUDIO)
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

    internal fun showLink(dialogTitle: String, dialogButtonText: String, resIdStyle: Int) {
        interactionListener?.onUserAction(TVUserAction.SHOW_QUALITY, false)

        val qualityList = currentMediaItem.linkList
            .filter { it.source == currentMediaItem.currentLink?.source }
            .map {
                AlertDialogItemView(
                    it.title,
                    if (it.isSelected) R.drawable.tv_ic_check else 0
                )
            }

        val qualityDialog = AlertDialogHelper(activity, resIdStyle, dialogTitle)
        qualityDialog.create(
            adapter = getAlertDialogAdapter(qualityList.toTypedArray()),
            itemClickListener = { self, position ->
                interactionListener?.onUserAction(TVUserAction.SELECT_QUALITY, false)
                val currentSource = currentMediaItem.currentLink?.source
                val targetLink =
                    currentMediaItem.links.filter { it.source == currentSource }[position]
                val targetIndex = currentMediaItem.links.indexOf(targetLink)
                if (!currentMediaItem.links[targetIndex].isSelected) changeQuality(targetIndex)
                else self.dismiss()
            },
            positiveButtonText = dialogButtonText,
            positiveClickListener = { self, _ -> self.dismiss() }
        )
        qualityDialog.show()
    }

    internal fun showSource(
        dialogTitle: String, dialogButtonText: String, resIdStyle: Int
    ) {
        interactionListener?.onUserAction(TVUserAction.SHOW_SOURCE)

        val qualityList = ArrayList<AlertDialogItemView>()
        val data = currentMediaItem.linkList.groupBy { it.source }
        data.forEach { (source, list) ->
            val isSelected = list.any { it.isSelected }
            qualityList.add(
                AlertDialogItemView(
                    source, if (isSelected) R.drawable.tv_ic_check else 0
                )
            )
        }

        val qualityDialog = AlertDialogHelper(activity, resIdStyle, dialogTitle)
        qualityDialog.create(adapter = getAlertDialogAdapter(qualityList.toTypedArray()),
            itemClickListener = { self, position ->
                val targetSource = currentMediaItem.links.distinctBy { it.source }[position].source
                val targetIndex = currentMediaItem.links.indexOfFirst { it.source == targetSource }
                if (data[targetSource]?.any { it.isSelected } == false) changeQuality(targetIndex)
                interactionListener?.onUserAction(TVUserAction.SELECT_SOURCE)
                self.dismiss()
            },
            positiveButtonText = dialogButtonText,
            positiveClickListener = { self, _ ->
                self.dismiss()
            })
        qualityDialog.show()
    }

    internal fun showQuality(
        dialogTitle: String,
        dialogButtonText: String,
        resIdStyle: Int,
        qualityDialogItemDefault: String
    ) {
        interactionListener?.onUserAction(TVUserAction.SHOW_QUALITY, true)
        val subtitleLanguageList = ArrayList<String>()
        val subtitlesList = ArrayList<AlertDialogItemView>()
        var groupIndex = -1
        var groupInfo: TrackGroup? = null
        player.currentTracks.groups.forEachIndexed { index, group ->
            if (group.type == C.TRACK_TYPE_VIDEO) {
                groupInfo = group.mediaTrackGroup
                groupIndex = index
                groupInfo?.let { safeGroupInfo ->
                    subtitleLanguageList.add(safeGroupInfo.getFormat(0).language.toString())
                    if (safeGroupInfo.getFormat(0).width > 0) {
                        for (i in 0 until safeGroupInfo.length) {
                            val format = safeGroupInfo.getFormat(i)
                            val qualityValue = min(format.width, format.height)
                            val quality = "${qualityValue}p"
                            val subtitleIcon =
                                if (group.isTrackSelected(i)) R.drawable.tv_ic_check else 0
                            val subtitleCheckSupported = group.isSupported
                            subtitlesList.add(
                                AlertDialogItemView(
                                    quality, subtitleIcon, subtitleCheckSupported
                                )
                            )
                        }
                    } else {
                        val subtitleIcon = R.drawable.tv_ic_check
                        val subtitleCheckSupported = group.isSupported
                        subtitlesList.add(
                            AlertDialogItemView(
                                qualityDialogItemDefault, subtitleIcon, subtitleCheckSupported
                            )
                        )
                    }
                }
            }
        }
        if (groupIndex < 0) return
        val dialog = AlertDialogHelper(activity, resIdStyle, dialogTitle)
        dialog.create(
            adapter = getAlertDialogAdapter(subtitlesList.toTypedArray()),
            itemClickListener = { _, position ->
                groupInfo?.let { selectStreamQuality(position, it) }
                interactionListener?.onUserAction(TVUserAction.SELECT_QUALITY, true)
            },
            positiveClickListener = { self, _ ->
                if (subtitlesList.size > 1) {
                    trackSelector.setParameters(
                        trackSelector.buildUponParameters().setRendererDisabled(
                            C.TRACK_TYPE_VIDEO, true
                        )
                    )
                }
                self.dismiss()
            },
            positiveButtonText = dialogButtonText
        )
        dialog.show()
    }

    fun selectStreamQuality(position: Int, trackGroup: TrackGroup) {
        trackSelector.setParameters(
            trackSelector.buildUponParameters().setOverrideForType(
                TrackSelectionOverride(
                    trackGroup, position
                )
            ).setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
        )
    }

    fun changeQuality(qualitySelectedPosition: Int) {
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
            currentMediaItem.changeQualityUriInItem(
                qualitySelectedPosition
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

    fun submitUserInteractionLogs(tvUserAction: TVUserAction) {
        interactionListener?.onUserAction(tvUserAction)
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