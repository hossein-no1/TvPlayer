package com.tv.player

import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.tv.core.base.BasePlayer
import com.tv.core.util.MediaItem
import com.tv.core.util.SubtitleItem
import com.tv.core.util.TvPlayerListener
import com.tv.player.databinding.ActivitySimplePlayerBinding
import com.tv.player.util.UrlHelper

class SimplePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimplePlayerBinding
    private lateinit var playerHandler: BasePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimplePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerHandler = BasePlayer.Builder(
            context = this,
            playerView = binding.tvPlayerViewActivitySimplePlayer
        ).createSimplePlayer(isLive = false)

        val subtitle1 = SubtitleItem(url = UrlHelper.subtitleUrl, label = "Subtitle 1")
        val subtitle2 = SubtitleItem(url = UrlHelper.subtitleUrl2, label = "Subtitle 2")

        val mediaWithoutSubtitle = MediaItem(UrlHelper.filmWithoutSubtitleLink)
        val mediaWithQuality = MediaItem(
            url = UrlHelper.film1080,
            subtitleItems = listOf(subtitle1, subtitle2),
            defaultQualityTitle = "Default quality"
        ).addQuality("720", UrlHelper.film720).addQuality("480", UrlHelper.film480)

        playerHandler.addListener(playerListener)
        playerHandler.addMediaList(listOf(mediaWithoutSubtitle, mediaWithQuality))
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