package com.tv.core.util.mediaItems

import android.net.Uri
import com.tv.core.util.episodelistdialog.EpisodeModel
import java.util.UUID

class EpisodeMediaItem(
    id: String = UUID.randomUUID().toString(),
    startPositionMs: Long = 0L,
    subtitleItems: List<SubtitleItem> = listOf(),
    dubbedList: List<DubbedItem> = listOf(),
    qualities: List<MediaLink> = listOf(),
    private val cover: String = ""
) : MediaItemParent(id, startPositionMs, subtitleItems, dubbedList, qualities) {

    internal fun convertToEpisodeModel(): EpisodeModel {
        return EpisodeModel(
            id = this.id,
            title = currentLink?.title ?: "",
            cover = this.cover,
            startPosition = this.startPositionMs
        )
    }

    override fun addLink(quality: String, link: String, adTagUri: Uri): EpisodeMediaItem {
        linkList.add(MediaLink(quality, link, adTagUri))
        if (linkList.size > 1) linkList.first().isSelected = true
        return this
    }

    override fun changeQualityUriInItem(qualitySelectedPosition: Int): EpisodeMediaItem {
        resetQualitySelected()
        linkList[qualitySelectedPosition].isSelected = true
        return this
    }

}