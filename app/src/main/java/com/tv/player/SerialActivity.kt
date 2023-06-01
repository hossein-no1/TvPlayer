package com.tv.player

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tv.core.base.TvPlayer
import com.tv.core.util.TvPlayBackException
import com.tv.core.util.TvPlayerListener
import com.tv.core.util.mediaItems.EpisodeMediaItem
import com.tv.core.util.mediaItems.MediaQuality
import com.tv.player.databinding.ActivitySerialBinding
import com.tv.player.util.UrlHelper

class SerialActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SerialActivity"
    }

    private lateinit var binding: ActivitySerialBinding
    private lateinit var playerHandler: TvPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySerialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerHandler = TvPlayer.Builder(
            activity = this,
            playerView = binding.playerView
        ).createSimplePlayer(isLive = false)

        val episodeMedia = mutableListOf<EpisodeMediaItem>()

        UrlHelper.episodeCoverList.forEachIndexed { index, coverUrl ->
            episodeMedia.add(
                EpisodeMediaItem(
                    cover = coverUrl,
                    qualities = listOf(
                        MediaQuality(
                            title = "Episode ${index + 1}",
                            link = UrlHelper.film720
                        )
                    )
                )
            )
        }

        playerHandler.addListener(playerListener)
        playerHandler.addMediaList(episodeMedia)
        playerHandler.prepareAndPlay()

    }

    private val playerListener = object : TvPlayerListener {

        override fun onPlayerError(error: TvPlayBackException) {
            super.onPlayerError(error)
            error.getErrorCodeMessage()
            if (error.errorCode == TvPlayBackException.ERROR_CODE_IO_BAD_HTTP_STATUS)
                Toast.makeText(
                    this@SerialActivity.applicationContext,
                    "Bad source error!",
                    Toast.LENGTH_SHORT
                ).show()
            Log.i(
                TAG,
                "ErrorMessage : ${error.errorMessage} and errorCode : ${error.errorCode} for ${playerHandler.getCurrentQuality()?.title}"
            )
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            binding.isLoading = playbackState == TvPlayer.STATE_BUFFERING
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.release()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return binding.playerView.handleDispatchKeyEvent(
            event,
            this
        )
    }

}