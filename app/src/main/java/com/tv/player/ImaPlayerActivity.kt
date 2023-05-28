package com.tv.player

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.tv.core.base.TvPlayer
import com.tv.core.util.mediaItems.MediaItem
import com.tv.core.util.mediaItems.MediaQuality
import com.tv.core.util.TvImaAdsLoader
import com.tv.core.util.TvPlayerListener
import com.tv.core.util.mediaItems.MediaItemParent
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
            .setLanguage("fr")
            .create()

        playerHandler = TvPlayer.Builder(
            activity = this,
            playerView = binding.tvPlayerViewActivityImaPlayer
        ).createImaPlayer(tvImaAdsLoader = imaAdsLoader)

        val media1 =
            MediaItem(
                qualities = listOf(
                    MediaQuality(
                        title = "720",
                        link = UrlHelper.film720,
                        adTagUri = Uri.parse("https://play-dev.huma.ir/api/ads/2")
                    )
                )
            )

        val media2 =
            MediaItem(
                qualities = listOf(
                    MediaQuality(
                        title = "480",
                        link = UrlHelper.film480
                    )
                )
            )

        playerHandler.addListener(playerListener)
        playerHandler.addMediaList(listOf(media1, media2))
        playerHandler.prepareAndPlay()

    }

    private val playerListener = object : TvPlayerListener{
        override fun onMediaStartToPlay(mediaItem: MediaItemParent) {
            super.onMediaStartToPlay(mediaItem)
            Log.i(TAG , "onMediaStartToPlay")
        }

        override fun onMediaComplete(mediaItem: MediaItemParent) {
            super.onMediaComplete(mediaItem)
            Log.i(TAG , "onMediaComplete")
        }

        override fun onMediaListComplete(mediaItem: MediaItemParent) {
            super.onMediaListComplete(mediaItem)
            Log.i(TAG , "onMediaListComplete")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.release()
    }

}