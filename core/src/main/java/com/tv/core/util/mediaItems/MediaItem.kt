package com.tv.core.util.mediaItems

import android.net.Uri
import java.util.UUID

class MediaItem(
    id: String = UUID.randomUUID().toString(),
    startPositionMs: Long = 0L,
    subtitleItems: List<SubtitleItem> = listOf(),
    dubbedList: List<DubbedItem> = listOf(),
    qualities: List<MediaQuality> = listOf()
) : MediaItemParent(id, startPositionMs, subtitleItems, dubbedList, qualities) {

    override fun addQuality(quality: String, link: String, adTagUri: Uri): MediaItemParent {
        qualityList.add(MediaQuality(quality, link, adTagUri))
        if (qualityList.size > 1) qualityList.first().isSelected = true
        return this
    }

    override fun changeQualityUriInItem(qualitySelectedPosition: Int): MediaItem {
        resetQualitySelected()
        qualityList[qualitySelectedPosition].isSelected = true
        return this
    }

}