package com.tv.core.util.mediaItems

import android.net.Uri
import com.google.android.exoplayer2.MediaItem.AdsConfiguration

internal object MediaItemConverter {
    fun convertMediaItem(mediaItem: MediaItemParent): com.google.android.exoplayer2.MediaItem {
        val exoMedia = com.google.android.exoplayer2.MediaItem.Builder()

        exoMedia
            .setMediaId(mediaItem.id)
            .setUri(mediaItem.currentQuality?.link)
            .setSubtitleConfigurations(SubtitleConverter.convertList(mediaItem.subtitleItems))

        mediaItem.currentQuality?.let { safeMediaItem ->
            if (safeMediaItem.adTagUri != Uri.EMPTY)
                exoMedia.setAdsConfiguration(
                    AdsConfiguration.Builder(
                        safeMediaItem.adTagUri
                    ).build()
                )
        }
        return exoMedia.build()
    }

    fun convertMediaList(mediaItems: List<MediaItem>): List<com.google.android.exoplayer2.MediaItem> {
        return mediaItems.map { convertMediaItem(it) }
    }

    fun convertAdvertiseItem(advertiseItem: AdvertiseItem): com.google.android.exoplayer2.MediaItem {
        val mediaItem = com.google.android.exoplayer2.MediaItem.Builder()
            .setMediaId(advertiseItem.id)
            .setUri(advertiseItem.url)
        advertiseItem.subtitleItem?.let { safeSubtitle ->
            mediaItem.setSubtitleConfigurations(listOf(SubtitleConverter.convert(safeSubtitle)))
        }
        return mediaItem.build()
    }
}