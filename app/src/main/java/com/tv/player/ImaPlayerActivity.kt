package com.tv.player

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.tv.core.base.TvPlayer
import com.tv.core.util.TVUserAction
import com.tv.core.util.TvImaAdsLoader
import com.tv.core.util.TvPlayerInteractionListener
import com.tv.core.util.TvPlayerListener
import com.tv.core.util.mediaItems.MediaItem
import com.tv.core.util.mediaItems.MediaItemParent
import com.tv.core.util.mediaItems.MediaLink
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
            .setLanguage("fa")
            .create()

        playerHandler = TvPlayer.Builder(
            activity = this,
            playerView = binding.tvPlayerViewActivityImaPlayer
        ).createImaPlayer(tvImaAdsLoader = imaAdsLoader)

        val media1 =
            MediaItem(
                links = listOf(
                    MediaLink(
                        title = "media1",
                        link = UrlHelper.streamLinkWithMoreQuality,
                        source = "Fam"
                    ),
                    MediaLink(
                        title = "media2",
                        link = UrlHelper.streamLinkWithMoreQuality,
                        source = "UP TV"
                    ),
                    MediaLink(
                        title = "media3",
                        link = UrlHelper.streamLinkWithMoreQuality,
                        source = "Lenz"
                    ),
                    MediaLink(
                        title = "media4",
                        link = UrlHelper.streamLinkWithMoreQuality,
                        source = "Lenz"
                    )
                )
            )
        val media2 =
            MediaItem(
                links = listOf(
                    MediaLink(
                        title = "آپرا - خودکار",
                        link = "https://traffic.upera.tv/2766450-0-hls.m3u8?ref=tUWI",
                        source = "upera"
                    ),
                    MediaLink(
                        title = "آپ تی وی - خودکار",
                        link = "https://onlines.uptvs.com/stream/movie/Inception_UPTV.co/all.m3u8",
                        source = "uptvs"
                    ),
                    MediaLink(
                        title = " دوبله فارسی - آپ تی وی - 1080",
                        link = "http://dl11.uptv.ir/uptv/film/Inception_1080p_UPTV.co.mkv",
                        source = "lenz"
                    ),
                    MediaLink(
                        title = " دوبله فارسی - آپ تی وی - 720",
                        link = "http://dl11.uptv.ir/uptv/film/Inception_720p_UPTV.co.mkv",
                        source = "lenz"
                    ),

                    )
            )
        playerHandler.addInteractionListener(interactionListener)
        playerHandler.addListener(playerListener)
        playerHandler.addMediaList(listOf(media2, media1))
        playerHandler.prepareAndPlay()

    }

    private val interactionListener = object : TvPlayerInteractionListener {
        override fun onUserAction(action: TVUserAction, data: Any?) {
            super.onUserAction(action, data)
            val isStream = data?.let {
                (it as Boolean)
            }
            Log.d(TAG, "onUserAction: TVUserAction:${action.value} isStream:${isStream}")
        }
    }

    private val playerListener = object : TvPlayerListener {
        override fun onMediaStartToPlay(mediaItem: MediaItemParent, currentMediaItemIndex: Int) {
            super.onMediaStartToPlay(mediaItem, currentMediaItemIndex)
            Log.i(TAG, "onMediaStartToPlay")
        }

        override fun onMediaComplete(mediaItem: MediaItemParent) {
            super.onMediaComplete(mediaItem)
            Log.i(TAG, "onMediaComplete")
        }

        override fun onMediaListComplete(mediaItem: MediaItemParent) {
            super.onMediaListComplete(mediaItem)
            Log.i(TAG, "onMediaListComplete")
        }

        override fun onControllerVisibilityChanged(visibility: Int) {
            super.onControllerVisibilityChanged(visibility)
            Log.i(TAG, "onControllerVisibilityChanged: $visibility")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.release()
    }

}