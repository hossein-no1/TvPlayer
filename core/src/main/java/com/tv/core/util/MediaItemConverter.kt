package com.tv.core.util

import com.google.android.exoplayer2.MediaItem.AdsConfiguration

internal object MediaItemConverter {
    fun convertMediaItem(mediaItem : MediaItem) : com.google.android.exoplayer2.MediaItem{
        return com.google.android.exoplayer2.MediaItem.Builder()
            .setMediaId(mediaItem.id)
            .setUri(mediaItem.url)
            .setSubtitleConfigurations(SubtitleConverter.convertList(mediaItem.subtitleItems))
            .setAdsConfiguration(AdsConfiguration.Builder(mediaItem.adTagUri).build())
            .build()
    }

    fun convertMediaList(mediaItems : List<MediaItem>) : List<com.google.android.exoplayer2.MediaItem>{
        return mediaItems.map { convertMediaItem(it) }
    }

    fun convertAdvertiseItem(advertiseItem: AdvertiseItem) : com.google.android.exoplayer2.MediaItem{
        val mediaItem = com.google.android.exoplayer2.MediaItem.Builder()
            .setMediaId(advertiseItem.id)
            .setUri(advertiseItem.url)
        advertiseItem.subtitleItem?.let { safeSubtitle ->
            mediaItem.setSubtitleConfigurations(listOf(SubtitleConverter.convert(safeSubtitle)))
        }
        return mediaItem.build()
    }
}