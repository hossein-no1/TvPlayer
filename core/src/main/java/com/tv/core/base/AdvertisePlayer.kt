package com.tv.core.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tv.core.util.AdvertisePlayerHandler
import java.util.*

class AdvertisePlayer(
    private val context: Context,
    private val playerView: PlayerView,
    private val adPlayerView: PlayerView,
    playWhenReady: Boolean = true
) : BasePlayer() {

    private var trackSelector: DefaultTrackSelector

    private var player: ExoPlayer
    private var adPlayer: ExoPlayer

    var advertisePlayerHandler: AdvertisePlayerHandler? = null
    private var skippAdvertiseTime = 10

    init {
        val videoTrackSelectionFactory =
            AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(context.applicationContext, videoTrackSelectionFactory)
        player = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
        playerView.player = player
        player.playWhenReady = playWhenReady

        adPlayer = ExoPlayer.Builder(context).build()
        adPlayerView.isEnabled = false
        adPlayerView.isFocusable = false
        adPlayerView.isClickable = false
        adPlayerView.player = adPlayer
        adPlayer.playWhenReady = true
    }

    override fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    override fun addMedia(media: MediaItem, index: Int) {
        player.addMediaItem(index, media)
    }

    override fun addMediaList(media: List<MediaItem>, index: Int) {
        player.addMediaItems(index, media)
    }

    override fun showSubtitle(overrideThemeResId: Int) {
        val subtitleLanguageList = ArrayList<String>()
        val subtitlesList = ArrayList<String>()
        for (group in player.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                val groupInfo = group.mediaTrackGroup
                for (i in 0 until groupInfo.length) {
                    subtitleLanguageList.add(groupInfo.getFormat(i).language.toString())
                    subtitlesList.add(
                        "${subtitlesList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage
                                + " (${
                            if (groupInfo.getFormat(i).label == null) "Subtitle" else groupInfo.getFormat(
                                i
                            ).label
                        })"
                    )
                }
            }
        }

        val tempTracks = subtitlesList.toArray(arrayOfNulls<CharSequence>(subtitlesList.size))
        val subtitleDialog =
            MaterialAlertDialogBuilder(context, overrideThemeResId)
                .setTitle("Select Subtitles")
                .setOnCancelListener { }
                .setPositiveButton("Off Subtitles") { self, _ ->
                    trackSelector.setParameters(
                        trackSelector.buildUponParameters().setRendererDisabled(
                            C.TRACK_TYPE_VIDEO, true
                        )
                    )
                    self.dismiss()
                }
                .setItems(tempTracks) { _, position ->
                    trackSelector.setParameters(
                        trackSelector.buildUponParameters()
                            .setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                            .setPreferredTextLanguage(subtitleLanguageList[position])
                    )
                }
                .create()
        subtitleDialog.show()
        subtitleDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        subtitleDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))

    }

    override fun isThereSubtitle(): Boolean {
        for (group in player.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                return true
            }
        }
        return false
    }

    override fun preparePlayer() {
        player.prepare()
    }

    override fun play() {
        player.play()
    }

    override fun release() {
        playerView.player = null
        player.release()
        adPlayerView.player = null
        adPlayer.release()
    }

    fun playAdvertiseAutomatic() {
        adPlayer.addListener(advertisePlayerEvent)
        adPlayerView.visibility = View.VISIBLE
        playerView.visibility = View.GONE
        adPlayer.prepare()
        adPlayer.play()
    }

    fun startForceVideo() {
        adPlayerView.player = null
        adPlayer.release()
        playerView.visibility = View.VISIBLE
        adPlayerView.visibility = View.GONE
        if (!player.isPlaying) {
            preparePlayer()
            play()
        }
    }

    fun addMediaAdvertise(media: MediaItem, skippTime: Int = 10, index: Int = 0) {
        this.skippAdvertiseTime = skippTime
        adPlayer.addMediaItem(index, media)
    }

    private val advertisePlayerEvent = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            advertisePlayerHandler?.playBackStateChange(playbackState)
            if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                finishAdvertise()
            } else if (playbackState == Player.STATE_READY) {
                handleSkippLoop()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            advertisePlayerHandler?.onPlayerError(error)
        }
    }

    private fun handleSkippLoop() {
        val timeToLeft = (skippAdvertiseTime - adPlayer.currentPosition / 1000).toInt()
        advertisePlayerHandler?.onSkippTimeChange(timeToLeft, skippAdvertiseTime)
        Handler(context.mainLooper).postDelayed(
            {
                if (timeToLeft > 0)
                    handleSkippLoop()
            }, 1000
        )
    }

    private fun finishAdvertise() {
        adPlayerView.visibility = View.GONE
        adPlayerView.player = null
        adPlayer.release()
        playMediaWhenAdvertiseFinished()
    }

    private fun playMediaWhenAdvertiseFinished() {
        playerView.visibility = View.VISIBLE
        preparePlayer()
        play()
    }

}