package com.tv.player

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tv.core.base.TvPlayer
import com.tv.core.util.TvDispatchKeyEvent
import com.tv.core.util.TvPlayBackException
import com.tv.core.util.TvPlayerListener
import com.tv.core.util.mediaItems.MediaItem
import com.tv.core.util.mediaItems.MediaQuality
import com.tv.core.util.mediaItems.SubtitleItem
import com.tv.player.databinding.ActivitySimplePlayerBinding
import com.tv.player.util.UrlHelper

class SimplePlayerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SimplePlayerActivity"
    }

    private lateinit var binding: ActivitySimplePlayerBinding
    private lateinit var playerHandler: TvPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimplePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerHandler = TvPlayer.Builder(
            activity = this,
            playerView = binding.tvPlayerViewActivitySimplePlayer
        ).createSimplePlayer(isLive = false)

        val subtitle1 = SubtitleItem(url = UrlHelper.subtitleUrl, label = "Subtitle 1")
        val subtitle2 = SubtitleItem(url = UrlHelper.subtitleUrl2, label = "Subtitle 2")

        val mediaWithoutSubtitle = MediaItem(
            qualities = listOf(
                MediaQuality(title = "Movie with Dubbed", link = UrlHelper.film720)
            ),
            dubbedList = listOf(UrlHelper.dubbed1, UrlHelper.dubbed2)
        )
        val mediaWithQuality = MediaItem(
            startPositionMs = 3_600_000L,
            subtitleItems = listOf(subtitle1, subtitle2),
            qualities = listOf(
                MediaQuality(title = "1080", link = UrlHelper.film1080)
            )
        )
            .addQuality("720", link = UrlHelper.film720)
            .addQuality("480", link = UrlHelper.film480)

        val mediaWithQualityList = MediaItem(
            qualities = listOf(
                MediaQuality(title = "720", link = UrlHelper.film720),
                MediaQuality(title = "1080", link = UrlHelper.film1080)
            )
        )

        playerHandler.addListener(playerListener)
        playerHandler.addMediaList(
            listOf(
                mediaWithoutSubtitle,
                mediaWithQuality,
                mediaWithQualityList
            )
        )
        playerHandler.prepareAndPlay()

    }

    private val playerListener = object : TvPlayerListener {

        override fun onPlayerError(error: TvPlayBackException) {
            super.onPlayerError(error)
            error.getErrorCodeMessage()
            if (error.errorCode == TvPlayBackException.ERROR_CODE_IO_BAD_HTTP_STATUS)
                Toast.makeText(
                    this@SimplePlayerActivity.applicationContext,
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
        return binding.tvPlayerViewActivitySimplePlayer.handleDispatchKeyEvent(
            event,
            this,
            customDispatcherKeyEvent
        )
    }

    private val customDispatcherKeyEvent = object : TvDispatchKeyEvent {
        override fun onEnterClick() {
            super.onEnterClick()
            // Do custom event on DPAD_CENTER click...

            playerHandler.pause()
        }
    }

}