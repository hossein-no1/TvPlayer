package com.tv.core.base

import android.content.Context
import android.os.Handler
import android.view.View
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.tv.core.ui.TvAdvertisePlayerView
import com.tv.core.ui.TvPlayerView
import com.tv.core.util.AdvertiseItem
import com.tv.core.util.AdvertisePlayerListener
import com.tv.core.util.MediaItemConverter

internal class AdvertisePlayer(
    private val context: Context,
    private val playerView: TvPlayerView,
    private val adPlayerView: TvAdvertisePlayerView,
    isLive : Boolean,
    playWhenReady: Boolean = true
) : BasePlayer(context, playerView, isLive, playWhenReady) {

    private var adPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private var advertiseListener: AdvertisePlayerListener? = null
    private var skipAdvertiseTime = 10

    init {
        setupElement()
        adPlayerView.isEnabled = false
        adPlayerView.isFocusable = false
        adPlayerView.isClickable = false
        adPlayerView.playerView.player = adPlayer
        adPlayer.playWhenReady = true
    }

    private fun setupElement(){
        adPlayerView.setupElement(this)
    }

    override fun release() {
        playerView.playerView.player = null
        player.release()
        adPlayerView.playerView.player = null
        adPlayer.release()
    }

    override fun playAdvertiseAutomatic() {
        adPlayer.addListener(advertisePlayerEvent)
        adPlayerView.visibility = View.VISIBLE
        playerView.visibility = View.GONE
        adPlayer.prepare()
        adPlayer.play()
    }

    override fun startForceVideo() {
        adPlayerView.playerView.player = null
        adPlayer.release()
        playerView.visibility = View.VISIBLE
        adPlayerView.visibility = View.GONE
        if (!player.isPlaying) {
            prepare()
            play()
        }
    }

    override fun addMediaAdvertise(media: AdvertiseItem, skippTime: Int, index: Int) {
        this.skipAdvertiseTime = skippTime
        adPlayer.addMediaItem(index, MediaItemConverter.convertAdvertiseItem(media))
    }

    override fun playAdvertise() {
        adPlayer.play()
    }

    override fun stopAdvertise() {
        adPlayer.stop()
    }

    override fun pauseAdvertise() {
        adPlayer.pause()
    }

    override fun setAdvertiseListener(adListener: AdvertisePlayerListener) {
        super.setAdvertiseListener(adListener)
        advertiseListener = adListener
    }

    private val advertisePlayerEvent = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            advertiseListener?.onPlaybackStateChanged(playbackState)
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
        advertiseListener?.onSkipTimeChange(timeToLeft, skipAdvertiseTime, adPlayerView.tvSkipAd)
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