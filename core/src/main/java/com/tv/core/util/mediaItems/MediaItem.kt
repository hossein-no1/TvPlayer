package com.tv.core.util.mediaItems

import android.net.Uri
import java.util.UUID

class MediaItem(
    id: String = UUID.randomUUID().toString(),
    startPositionMs: Long = 0L,
    subtitleItems: List<SubtitleItem> = listOf(),
    dubbedList: List<DubbedItem> = listOf(),
    links: List<MediaLink> = listOf()
) : MediaItemParent(id, startPositionMs, subtitleItems, dubbedList, links) {

    override fun addLink(quality: String, link: String, adTagUri: Uri): MediaItemParent {
        linkList.add(MediaLink(quality, link, adTagUri))
        if (linkList.size > 1) linkList.first().isSelected = true
        return this
    }

    override fun changeQualityUriInItem(qualitySelectedPosition: Int): MediaItem {
        resetQualitySelected()
        linkList[qualitySelectedPosition].isSelected = true
        return this
    }

}