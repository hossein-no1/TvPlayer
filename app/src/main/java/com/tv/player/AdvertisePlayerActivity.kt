package com.tv.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.tv.core.base.TvPlayer
import com.tv.core.util.*
import com.tv.core.util.mediaItems.AdvertiseItem
import com.tv.core.util.mediaItems.MediaItem
import com.tv.core.util.mediaItems.MediaQuality
import com.tv.player.databinding.ActivityAdvertisePlayerBinding
import com.tv.player.util.UrlHelper

class AdvertisePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdvertisePlayerBinding
    private lateinit var playerHandler: TvPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvertisePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerHandler = TvPlayer.Builder(
            activity = this,
            playerView = binding.playerViewActivityAdvertisePlayer,
            /* Optional */playWhenReady = true
        ).createAdvertisePlayer(
            adPlayerView = binding.advertisePlayerViewActivityAdvertiserPlayer,
            isLive = false
        )

        val adMedia = AdvertiseItem(url = UrlHelper.ad)

        val media = MediaItem(qualities = listOf(MediaQuality(link = UrlHelper.film720)))

        playerHandler.addMediaAdvertise(adMedia)
        playerHandler.addMedia(media)
        playerHandler.addListener(playerListener)
        playerHandler.setAdvertiseListener(advertiseListener)
        playerHandler.playAdvertiseAutomatic()

    }

    private val advertiseListener = object : AdvertisePlayerListener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            //Handle loading
            binding.isLoading = playbackState == TvPlayer.STATE_BUFFERING
        }

        override fun onPlayerError(error: TvPlayBackException) {
            super.onPlayerError(error)
            error.printStackTrace()
        }

        @SuppressLint("SetTextI18n")
        override fun onSkipTimeChange(
            currentTime: Int,
            skippTime: Int,
            textViewSkip: AppCompatTextView?
        ) {
            textViewSkip?.apply {
                this.visibility = View.VISIBLE
                this.text = " تا رد کردن آگهی$currentTime"
                if (currentTime <= 0)
                    this.text = "ردکردن آگهی"
            }
        }
    }

    private val playerListener = object : TvPlayerListener {
        override fun onPlayerError(error: TvPlayBackException) {
            super.onPlayerError(error)
            error.printStackTrace()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            binding.isLoading = playbackState == TvPlayer.STATE_BUFFERING
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        playerHandler.release()
    }

}