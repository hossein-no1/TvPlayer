package com.tv.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.tv.core.base.TvPlayer
import com.tv.core.util.AdvertiseItem
import com.tv.core.util.AdvertisePlayerListener
import com.tv.core.util.MediaItem
import com.tv.core.util.TvPlayerListener
import com.tv.player.databinding.ActivityAdvertisePlayerBinding
import com.tv.player.util.UrlHelper

class AdvertisePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdvertisePlayerBinding
    private lateinit var playerHelper: TvPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvertisePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerHelper = TvPlayer.Builder(
            activity = this,
            playerView = binding.playerViewActivityAdvertisePlayer,
            /* Optional */playWhenReady = true
        ).createAdvertisePlayer(adPlayerView = binding.advertisePlayerViewActivityAdvertiserPlayer, isLive = false)

        val adMedia = AdvertiseItem(url = UrlHelper.ad)

        val media = MediaItem(url = UrlHelper.film720)

        playerHelper.addMediaAdvertise(adMedia)
        playerHelper.addMedia(media)
        playerHelper.addListener(playerListener)
        playerHelper.setAdvertiseListener(advertiseListener)
        playerHelper.playAdvertiseAutomatic()

    }

    private val advertiseListener = object : AdvertisePlayerListener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            //Handle loading
            binding.isLoading = playbackState == TvPlayer.STATE_BUFFERING
        }

        override fun onPlayerError(error: Exception) {
            error.printStackTrace()
        }

        @SuppressLint("SetTextI18n")
        override fun onSkipTimeChange(currentTime: Int, skippTime: Int, textViewSkip : AppCompatTextView?) {
            textViewSkip?.apply {
                this.visibility = View.VISIBLE
                this.text = " تا رد کردن آگهی$currentTime"
                if (currentTime <= 0)
                    this.text = "ردکردن آگهی"
            }
        }
    }

    private val playerListener = object : TvPlayerListener {
        override fun onPlayerError(error: Exception) {
            super.onPlayerError(error)
            error.printStackTrace()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            binding.isLoading = playbackState == TvPlayer.STATE_BUFFERING
            if (playbackState == TvPlayer.STATE_READY)
                findViewById<RelativeLayout>(com.tv.core.R.id.parent_pauseAndPlay)?.requestFocus()
        }
    }

}