package com.tv.core.util.mediaItems

import android.net.Uri
import com.tv.core.util.episodelistdialog.EpisodeModel
import java.util.UUID

class EpisodeMediaItem(
    id: String = UUID.randomUUID().toString(),
    startPositionMs: Long = 0L,
    subtitleItems: List<SubtitleItem> = listOf(),
    dubbedList: List<DubbedItem> = listOf(),
    qualities: List<MediaQuality> = listOf(),
    private val cover: String = ""
) : MediaItemParent(id, startPositionMs, subtitleItems, dubbedList, qualities) {

    internal fun convertToEpisodeModel(): EpisodeModel {
        return EpisodeModel(
            id = this.id,
            title = currentQuality?.title ?: "",
            cover = this.cover,
            startPosition = this.startPositionMs
        )
    }

    override fun addQuality(quality: String, link: String, adTagUri: Uri): EpisodeMediaItem {
        qualityList.add(MediaQuality(quality, link, adTagUri))
        if (qualityList.size > 1) qualityList.first().isSelected = true
        return this
    }

    override fun changeQualityUriInItem(qualitySelectedPosition: Int): EpisodeMediaItem {
        resetQualitySelected()
        qualityList[qualitySelectedPosition].isSelected = true
        return this
    }

}