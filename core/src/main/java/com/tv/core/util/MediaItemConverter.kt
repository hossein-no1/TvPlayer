package com.tv.core.util

object MediaItemConverter {
    fun convert(mediaItem : MediaItem) : com.google.android.exoplayer2.MediaItem{
        return com.google.android.exoplayer2.MediaItem.Builder()
            .setUri(mediaItem.uri)
            .setTag(if (mediaItem.isLive) MediaSourceType.Hls else MediaSourceType.Progressive)
            .setSubtitleConfigurations(SubtitleConverter.convertList(mediaItem.subtitleConfiguration))
            .build()
    }

    fun convertList(mediaItems : List<MediaItem>) : List<com.google.android.exoplayer2.MediaItem>{
        return mediaItems.map { convert(it) }
    }
}