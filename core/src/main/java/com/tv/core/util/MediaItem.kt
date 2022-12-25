package com.tv.core.util

import android.net.Uri

class MediaItem(
    var uri: Uri,
    val subtitleConfiguration: List<SubtitleItem> = listOf(),
    val isLive: Boolean = false,
    defaultQualityTitle: String = "Default",
) {

    private val qualityList: MutableList<MediaQuality> =
        mutableListOf(MediaQuality(defaultQualityTitle, uri))

    fun addQuality(quality: String, uri: Uri): MediaItem {
        qualityList.add(MediaQuality(quality, uri))
        if (qualityList.size > 1)
            qualityList.first().isSelected = true
        return this
    }

    fun getQualityList(): List<MediaQuality> = qualityList

    fun isThereQuality() = qualityList.size > 1

}

class MediaQuality(
    val title: String,
    val link: Uri,
    var isSelected: Boolean = false
)