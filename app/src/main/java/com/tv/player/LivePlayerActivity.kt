package com.tv.player

import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.tv.core.base.BasePlayer
import com.tv.core.base.LivePlayer
import com.tv.core.util.MediaItem
import com.tv.core.util.TvPlayerListener
import com.tv.player.databinding.ActivityLivePlayerBinding
import com.tv.player.util.UrlHelper

class LivePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLivePlayerBinding
    private lateinit var playerHandler: LivePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerHandler = LivePlayer(
            context = this,
            playerView = binding.livePlayerViewActivityLivePlayer
        )

        val media = MediaItem(url = UrlHelper.linkLive, isLive = true)
        playerHandler.addListener(playerListener)
        playerHandler.addMedia(media)
        playerHandler.prepareAndPlay()

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

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.release()
    }

}