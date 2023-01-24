package com.tv.core.base

import android.app.Activity
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
    private val activity: Activity,
    private val tvPlayerView: TvPlayerView,
    private val tvAdvertisePlayerView: TvAdvertisePlayerView,
    isLive : Boolean,
    playWhenReady: Boolean = true
) : TvPlayer(activity, tvPlayerView, isLive, playWhenReady) {

    private var adPlayer: ExoPlayer = ExoPlayer.Builder(activity.applicationContext).build()

    private var advertiseListener: AdvertisePlayerListener? = null
    private var skipAdvertiseTime = 10

    init {
        setupElement()
        tvAdvertisePlayerView.isEnabled = false
        tvAdvertisePlayerView.isFocusable = false
        tvAdvertisePlayerView.isClickable = false
        tvAdvertisePlayerView.playerView.player = adPlayer
        adPlayer.playWhenReady = true
    }

    private fun setupElement(){
        tvAdvertisePlayerView.setupElement(this)
    }

    override fun release() {
        tvPlayerView.playerView.player = null
        player.release()
        tvAdvertisePlayerView.playerView.player = null
        adPlayer.release()
    }

    override fun playAdvertiseAutomatic() {
        adPlayer.addListener(advertisePlayerEvent)
        tvAdvertisePlayerView.visibility = View.VISIBLE
        tvPlayerView.visibility = View.GONE
        adPlayer.prepare()
        adPlayer.play()
    }

    override fun startForceVideo() {
        tvAdvertisePlayerView.playerView.player = null
        adPlayer.release()
        tvPlayerView.visibility = View.VISIBLE
        tvAdvertisePlayerView.visibility = View.GONE
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
        advertiseListener?.onSkipTimeChange(timeToLeft, skipAdvertiseTime, tvAdvertisePlayerView.tvSkipAd)
        Handler(activity.applicationContext.mainLooper).postDelayed(
            {
                if (timeToLeft > 0 && adPlayer.isPlaying) {
                    handleSkippLoop()
                }
            }, 1000
        )
    }

    private fun finishAdvertise() {
        tvAdvertisePlayerView.visibility = View.GONE
        tvAdvertisePlayerView.playerView.player = null
        adPlayer.release()
        playMediaWhenAdvertiseFinished()
    }

    private fun playMediaWhenAdvertiseFinished() {
        tvPlayerView.visibility = View.VISIBLE
        prepare()
        play()
    }

}