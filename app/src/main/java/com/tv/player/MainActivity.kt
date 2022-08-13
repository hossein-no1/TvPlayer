package com.tv.player

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.util.MimeTypes
import com.tv.player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivityLogCat"

    private val videoUrl720 = "http://dl.gemescape.com/film/2022/Elvis/Elvis.2022.720p.WEBRip.x264.AAC.YTS.PK.mp4"

    private lateinit var binding : ActivityMainBinding
    private lateinit var player : ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = ExoPlayer.Builder(this).build()

        binding.playerView.player = player

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(videoUrl720))
            .build()

        player.addListener(playerListener)

        player.addMediaItem(mediaItem)
        player.prepare()
        player.play()


    }

    private val playerListener = object : Player.Listener{
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.i(TAG, "onPlayerError: ${error.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}