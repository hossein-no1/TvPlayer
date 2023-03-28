package com.tv.player

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tv.core.base.TvPlayer
import com.tv.core.util.MediaItem
import com.tv.core.util.SubtitleItem
import com.tv.core.util.TvPlayBackException
import com.tv.core.util.TvPlayerListener
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
            url = UrlHelper.filmWithDubbed,
            dubbedList = listOf(UrlHelper.dubbed1, UrlHelper.dubbed2)
        )
        val mediaWithQuality = MediaItem(
            url = UrlHelper.film1080,
            startPositionMs = 3_600_000L,
            subtitleItems = listOf(subtitle1, subtitle2),
            defaultQualityTitle = "Default quality"
        ).addQuality("720", UrlHelper.film720).addQuality("480", UrlHelper.film480)

        val mediaWithQualityList = MediaItem(
            url = UrlHelper.film1080,
            qualities = listOf(
                Pair("Item 1", UrlHelper.film720),
                Pair("Item 2", UrlHelper.film1080)
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
                "ErrorMessage : ${error.errorMessage} and errorCode : ${error.errorCode} for ${playerHandler.currentMediaItem.url}"
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
        return binding.tvPlayerViewActivitySimplePlayer.handleDispatchKeyEvent(event, this)
    }

}