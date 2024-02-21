package com.tv.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tv.core.base.TvPlayer
import com.tv.core.util.mediaItems.MediaItem
import com.tv.core.util.mediaItems.MediaLink
import com.tv.core.util.TvPlayBackException
import com.tv.core.util.TvPlayerListener
import com.tv.core.util.mediaItems.MediaItemParent
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

        val media = MediaItem(
            links = listOf(MediaLink(title = "Live", link = UrlHelper.linkLive))
        )
        playerHandler.addListener(playerListener)
        playerHandler.addMedia(media)
        playerHandler.prepareAndPlay()

    }

    private val playerListener = object : TvPlayerListener {
        override fun onPlayerError(
            error: TvPlayBackException,
            currentMediaItem: MediaItemParent,
            currentMediaItemIndex: Int
        ) {
            super.onPlayerError(error, currentMediaItem, currentMediaItemIndex)
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