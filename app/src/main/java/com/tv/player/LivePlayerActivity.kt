package com.tv.player

import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.tv.core.base.TvPlayer
import com.tv.core.util.MediaItem
import com.tv.core.util.TvPlayerListener
import com.tv.player.databinding.ActivityLivePlayerBinding
import com.tv.player.util.UrlHelper

class LivePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLivePlayerBinding
    private lateinit var playerHandler: TvPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerHandler = TvPlayer.Builder(
            activity = this,
            playerView = binding.livePlayerViewActivityLivePlayer
        ).createSimplePlayer(isLive = true)

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
            binding.isLoading = playbackState == TvPlayer.STATE_BUFFERING
            if (playbackState == TvPlayer.STATE_READY)
                findViewById<RelativeLayout>(com.tv.core.R.id.parent_pauseAndPlay)?.requestFocus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.release()
    }

}