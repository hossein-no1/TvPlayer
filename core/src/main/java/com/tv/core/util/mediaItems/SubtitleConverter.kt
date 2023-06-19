package com.tv.core.util.mediaItems

import android.net.Uri
import com.google.android.exoplayer2.C.ROLE_FLAG_SUBTITLE
import com.google.android.exoplayer2.MediaItem.SubtitleConfiguration

internal object SubtitleConverter {
    fun convert(subtitleItem: SubtitleItem): SubtitleConfiguration {
        return SubtitleConfiguration.Builder(Uri.parse(subtitleItem.url))
            .setLanguage(subtitleItem.language)
            .setLabel(subtitleItem.label)
            .setMimeType(subtitleItem.mimeType)
            .setSelectionFlags(subtitleItem.selectionFlags)
            .setRoleFlags(ROLE_FLAG_SUBTITLE)
            .build()
    }

    fun convertList(subtitleItems : List<SubtitleItem>) : List<SubtitleConfiguration>{
        return subtitleItems.map { convert(it) }
    }
}