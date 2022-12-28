package com.tv.core.base

import android.content.Context
import android.os.Handler
import android.view.View
import com.google.android.exoplayer2.ExoPlayer
import com.tv.core.util.MediaItem as TvMediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.tv.core.ui.TvAdvertisePlayerView
import com.tv.core.ui.TvPlayerView
import com.tv.core.util.AdvertiseItem
import com.tv.core.util.AdvertisePlayerListener
import com.tv.core.util.MediaItemConverter

class AdvertisePlayer(
    private val context: Context,
    private val playerView: TvPlayerView,
    private val adPlayerView: TvAdvertisePlayerView,
    playWhenReady: Boolean = true
) : BasePlayer(context, playerView, playWhenReady) {

    private var adPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    var advertiseListener: AdvertisePlayerListener? = null
    private var skipAdvertiseTime = 10

    init {
        adPlayerView.setupElement(this)
        adPlayerView.isEnabled = false
        adPlayerView.isFocusable = false
        adPlayerView.isClickable = false
        adPlayerView.playerView.player = adPlayer
        adPlayer.playWhenReady = true
    }

    override fun release() {
        playerView.playerView.player = null
        player.release()
        adPlayerView.playerView.player = null
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
        adPlayerView.playerView.player = null
        adPlayer.release()
        playerView.visibility = View.VISIBLE
        adPlayerView.visibility = View.GONE
        if (!player.isPlaying) {
            prepare()
            play()
        }
    }

    fun addMediaAdvertise(media: AdvertiseItem, skippTime: Int = 10, index: Int = 0) {
        this.skipAdvertiseTime = skippTime
        adPlayer.addMediaItem(index, MediaItemConverter.convertAdvertiseItem(media))
    }

    fun playAdvertise() {
        adPlayer.play()
    }

    fun stopAdvertise() {
        adPlayer.stop()
    }

    fun pauseAdvertise() {
        adPlayer.pause()
    }

    private val advertisePlayerEvent = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            advertiseListener?.playBackStateChange(playbackState)
            if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                finishAdvertise()
            } else if (playbackState == Player.STATE_READY) {
                handleSkippLoop()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            advertiseListener?.onPlayerError(error)
        }
    }

    private fun handleSkippLoop() {
        val timeToLeft =
            if (skipAdvertiseTime <= 0) ((adPlayer.duration / 1000) - adPlayer.currentPosition / 1000).toInt() else (skipAdvertiseTime - adPlayer.currentPosition / 1000).toInt()
        advertiseListener?.onSkipTimeChange(timeToLeft, skipAdvertiseTime)
        Handler(context.mainLooper).postDelayed(
            {
                if (timeToLeft > 0 && adPlayer.isPlaying) {
                    handleSkippLoop()
                }
            }, 1000
        )
    }

    private fun finishAdvertise() {
        adPlayerView.visibility = View.GONE
        adPlayerView.playerView.player = null
        adPlayer.release()
        playMediaWhenAdvertiseFinished()
    }

    private fun playMediaWhenAdvertiseFinished() {
        playerView.visibility = View.VISIBLE
        prepare()
        play()
    }

}