package com.tv.player

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.tv.core.base.TvPlayer
import com.tv.core.util.MediaItem
import com.tv.core.util.MediaQuality
import com.tv.core.util.TvImaAdsLoader
import com.tv.player.databinding.ActivityImaPlayerBinding
import com.tv.player.util.UrlHelper

class ImaPlayerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ImaPlayerActivity"
    }

    private lateinit var binding: ActivityImaPlayerBinding
    private lateinit var playerHandler: TvPlayer
    private lateinit var imaAdsLoader: TvImaAdsLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImaPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imaAdsLoader = TvImaAdsLoader(this)
            .setAdErrorListener { Log.i(TAG, "Error : ${it?.error?.message}") }
            .setAdEventListener { Log.i(TAG, it?.ad?.title.toString()) }
            .setVideoAdPlayerCallBack(object : VideoAdPlayer.VideoAdPlayerCallback {
                override fun onAdProgress(p0: AdMediaInfo?, p1: VideoProgressUpdate?) {
                    Log.i(TAG, "onAdProgress")
                }

                override fun onBuffering(p0: AdMediaInfo?) {
                    Log.i(TAG, "onBuffering")
                }

                override fun onContentComplete() {
                    Log.i(TAG, "onContentComplete")
                }

                override fun onEnded(p0: AdMediaInfo?) {
                    Log.i(TAG, "onEnded")
                }

                override fun onError(p0: AdMediaInfo?) {
                    Log.i(TAG, "onError")
                }

                override fun onLoaded(p0: AdMediaInfo?) {
                    Log.i(TAG, "onLoaded")
                }

                override fun onPause(p0: AdMediaInfo?) {
                    Log.i(TAG, "onPause")
                }

                override fun onPlay(p0: AdMediaInfo?) {
                    Log.i(TAG, "onPlay")
                }

                override fun onResume(p0: AdMediaInfo?) {
                    Log.i(TAG, "onResume")
                }

                override fun onVolumeChanged(p0: AdMediaInfo?, p1: Int) {
                    Log.i(TAG, "onVolumeChanged")
                }

            })
            .create()

        playerHandler = TvPlayer.Builder(
            activity = this,
            playerView = binding.tvPlayerViewActivityImaPlayer
        ).createImaPlayer(tvImaAdsLoader = imaAdsLoader)

        val media =
            MediaItem(
                qualities = listOf(
                    MediaQuality(
                        title = "720",
                        link = UrlHelper.film720,
                        adTagUri = Uri.parse(UrlHelper.SAMPLE_VAST_TAG_URL_2)
                    )
                )
            )

        playerHandler.addMedia(media)
        playerHandler.prepareAndPlay()

    }

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.release()
    }

}