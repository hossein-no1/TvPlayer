package com.tv.core.util

import com.google.android.exoplayer2.MediaItem.SubtitleConfiguration

object SubtitleConverter {
    fun convert(subtitleItem: SubtitleItem): SubtitleConfiguration {
        return SubtitleConfiguration.Builder(subtitleItem.uri)
            .setLanguage(subtitleItem.language)
            .setLabel(subtitleItem.label)
            .setMimeType(subtitleItem.mimeType)
            .build()
    }

    fun convertList(subtitleItems : List<SubtitleItem>) : List<SubtitleConfiguration>{
        return subtitleItems.map { convert(it) }
    }
}