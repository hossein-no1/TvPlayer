package com.tv.player

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.tv.core.base.AdvertisePlayer
import com.tv.core.base.BasePlayer
import com.tv.core.util.AdvertiseItem
import com.tv.core.util.AdvertisePlayerListener
import com.tv.core.util.MediaItem
import com.tv.core.util.TvPlayerListener
import com.tv.player.databinding.ActivityAdvertisePlayerBinding
import com.tv.player.util.UrlHelper

class AdvertisePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdvertisePlayerBinding
    private lateinit var playerHelper: AdvertisePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvertisePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerHelper = AdvertisePlayer(
            context = this,
            playerView = binding.playerViewActivityAdvertisePlayer,
            adPlayerView = binding.advertisePlayerViewActivityAdvertiserPlayer,
            /* Optional */playWhenReady = true
        )

        val adMedia = AdvertiseItem(url = UrlHelper.ad)

        val media = MediaItem(url = UrlHelper.film720)

        playerHelper.addMediaAdvertise(adMedia)
        playerHelper.addMedia(media)
        playerHelper.addListener(playerListener)
        playerHelper.advertiseListener = advertiseListener
        playerHelper.playAdvertiseAutomatic()

    }

    private val advertiseListener = object : AdvertisePlayerListener {

        override fun playBackStateChange(playbackState: Int) {
            super.playBackStateChange(playbackState)
        }

        override fun onPlayerError(error: Exception) {
            error.printStackTrace()
        }

        override fun onSkipTimeChange(currentTime: Int, skippTime: Int) {
            findViewById<AppCompatTextView>(R.id.skippAd).apply {
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
            binding.isLoading = playbackState == BasePlayer.STATE_BUFFERING
            if (playbackState == BasePlayer.STATE_READY)
                findViewById<RelativeLayout>(R.id.parent_pauseAndPlay)?.requestFocus()
        }
    }

}