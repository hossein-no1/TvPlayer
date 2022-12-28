package com.tv.core.util

class MediaItem(
    var url: String,
    val subtitleItems: List<SubtitleItem> = listOf(),
    val isLive: Boolean = false,
    defaultQualityTitle: String = "Default",
) {

    private val qualityList: MutableList<MediaQuality> =
        mutableListOf(MediaQuality(defaultQualityTitle, url))

    fun addQuality(quality: String, url: String): MediaItem {
        qualityList.add(MediaQuality(quality, url))
        if (qualityList.size > 1)
            qualityList.first().isSelected = true
        return this
    }

    fun getQualityList(): List<MediaQuality> = qualityList

    fun isThereQuality() = qualityList.size > 1

}

class MediaQuality(
    val title: String,
    val link: String,
    var isSelected: Boolean = false
)